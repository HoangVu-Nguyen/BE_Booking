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
                // Giải phóng ổ cứng 20GB vừa nâng cấp cho Jenkins
                sh "docker image prune -a -f"
            }
        }

        stage('Deploy to EC2 BE') {
            steps {
                sshagent([env.SSH_KEY_ID]) {
                    withCredentials([usernamePassword(credentialsId: env.DOCKER_HUB_CREDS, usernameVariable: 'DUSER', passwordVariable: 'DPASS')]) {
                        sh """
                            # 1. Tạo thư mục và đẩy file cấu hình (Không cần đẩy override lên Prod nếu không dùng)
                            ssh -o StrictHostKeyChecking=no ${TARGET_USER}@${TARGET_IP} "mkdir -p ${TARGET_DIR}"
                            scp -o StrictHostKeyChecking=no docker-compose.yml docker-compose.prod.yml ${TARGET_USER}@${TARGET_IP}:${TARGET_DIR}/

                            ssh -o StrictHostKeyChecking=no ${TARGET_USER}@${TARGET_IP} "
                                cd ${TARGET_DIR} && \
                                echo '${DPASS}' | docker login -u ${DUSER} --password-stdin && \

                                # 2. Định nghĩa biến môi trường cho Docker Compose
                                export IMAGE_NAME=${REGISTRY} && \
                                export BUILD_NUMBER=${BUILD_NUMBER} && \

                                # 3. Chỉ dùng file gốc và prod để tránh bị dính profile 'manual' từ override
                                docker compose -f docker-compose.yml -f docker-compose.prod.yml down --remove-orphans && \

                                # 4. Kéo image app bản mới nhất về
                                docker compose -f docker-compose.yml -f docker-compose.prod.yml pull app && \

                                # 5. Khởi chạy toàn bộ hệ thống
                                docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d && \

                                # 6. Dọn dẹp image cũ trên máy BE
                                docker image prune -a -f
                            "
                        """
                    }
                }
            }
        }
    }
}