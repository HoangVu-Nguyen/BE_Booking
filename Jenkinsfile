pipeline {
    agent any

    environment {
        // Cấu hình Docker Hub
        DOCKER_HUB_USER = "clyvasync" // <--- Thay bằng username của bạn
        IMAGE_NAME      = "clyvasync-be"
        REGISTRY        = "${DOCKER_HUB_USER}/${IMAGE_NAME}"
        DOCKER_HUB_CREDS = 'docker-hub-credentials'

        // Cấu hình EC2 BE
        TARGET_USER     = "ubuntu"
        TARGET_IP       = "3.236.101.255"
        TARGET_DIR      = "/home/ubuntu/clyvasync"
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
                    // Build image với tag là số của lần build (BUILD_NUMBER) và tag 'latest'
                    sh "docker build -t ${REGISTRY}:${BUILD_NUMBER} ."
                    sh "docker build -t ${REGISTRY}:latest ."
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    // Login và Push lên Docker Hub
                    withCredentials([usernamePassword(credentialsId: env.DOCKER_HUB_CREDS, usernameVariable: 'DUSER', passwordVariable: 'DPASS')]) {
                        sh "echo \$DPASS | docker login -u \$DUSER --password-stdin"
                        sh "docker push ${REGISTRY}:${BUILD_NUMBER}"
                        sh "docker push ${REGISTRY}:latest"
                    }
                }
            }
        }

        stage('Deploy to EC2 BE') {
            steps {
                sshagent([env.SSH_KEY_ID]) {
                    sh """
                        # Tạo thư mục và chép file compose sang máy BE
                        ssh -o StrictHostKeyChecking=no ${TARGET_USER}@${TARGET_IP} "mkdir -p ${TARGET_DIR}"
                        scp -o StrictHostKeyChecking=no docker-compose.yml docker-compose.prod.yml docker-compose.override.yml ${TARGET_USER}@${TARGET_IP}:${TARGET_DIR}/

                        # Điều khiển máy BE kéo image mới về và chạy
                        ssh -o StrictHostKeyChecking=no ${TARGET_USER}@${TARGET_IP} "
                            cd ${TARGET_DIR} && \
                            docker login -u ${DOCKER_HUB_USER} -p YOUR_DOCKER_HUB_TOKEN_OR_PASS && \
                            docker compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.override.yml pull && \
                            docker compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.override.yml up -d && \
                            docker image prune -f
                        "
                    """
                }
            }
        }
    }
}