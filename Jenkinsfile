pipeline {
    agent any

    environment {
        DOCKER_HUB_USER = "clyvasync"
        IMAGE_NAME      = "clyvasync-be"
        REGISTRY        = "${DOCKER_HUB_USER}/${IMAGE_NAME}"
        DOCKER_HUB_CREDS = 'docker-hub-credentials'

        TARGET_USER     = "ubuntu"
        TARGET_IP       = "3.236.101.255"
        TARGET_DIR      = "/home/ubuntu/app/clyvasync"
        SSH_KEY_ID      = "ec2-server-key"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // Tận dụng BUILD_NUMBER để quản lý phiên bản
                    sh "docker build -t ${REGISTRY}:${BUILD_NUMBER} ."
                    sh "docker build -t ${REGISTRY}:latest ."
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: env.DOCKER_HUB_CREDS, usernameVariable: 'DUSER', passwordVariable: 'DPASS')]) {
                        sh "echo \$DPASS | docker login -u \$DUSER --password-stdin"
                        sh "docker push ${REGISTRY}:${BUILD_NUMBER}"
                        sh "docker push ${REGISTRY}:latest"
                    }
                }
            }
        }

        stage('Local Cleanup') {
            steps {
                // Xóa image vừa build xong trên máy Jenkins để giải phóng 8GB ổ cứng
                sh "docker image prune -a -f"
            }
        }

        stage('Deploy to EC2 BE') {
                    steps {
                        sshagent([env.SSH_KEY_ID]) {
                            withCredentials([usernamePassword(credentialsId: env.DOCKER_HUB_CREDS, usernameVariable: 'DUSER', passwordVariable: 'DPASS')]) {
                                sh """
                                    # Đẩy file cấu hình sang máy BE
                                    ssh -o StrictHostKeyChecking=no ${TARGET_USER}@${TARGET_IP} "mkdir -p ${TARGET_DIR}"
                                    scp -o StrictHostKeyChecking=no docker-compose.yml docker-compose.prod.yml docker-compose.override.yml ${TARGET_USER}@${TARGET_IP}:${TARGET_DIR}/

                                    ssh -o StrictHostKeyChecking=no ${TARGET_USER}@${TARGET_IP} "
                                        cd ${TARGET_DIR} && \
                                        echo '${DPASS}' | docker login -u ${DUSER} --password-stdin && \

                                        # Dọn dẹp container cũ trước khi chạy bản mới
                                        docker compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.override.yml down --remove-orphans && \

                                        # QUAN TRỌNG: Gán BUILD_TAG bằng số lần build của Jenkins
                                        export IMAGE_NAME=${DOCKER_HUB_USER}/${IMAGE_NAME} && \
                                        export BUILD_TAG=${BUILD_NUMBER} && \

                                        # Pull image có tag cụ thể vừa build xong
                                        docker compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.override.yml pull && \

                                        # Khởi chạy
                                        docker compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.override.yml up -d && \

                                        docker image prune -a -f
                                    "
                                """
                            }
                        }
                    }
                }
    }
}