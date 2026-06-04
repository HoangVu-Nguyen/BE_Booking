pipeline {
    agent any

    environment {
        DOCKER_HUB_USER = "clyvasync"
        IMAGE_NAME      = "booking-be"
        IMAGE_TAG       = "${BUILD_NUMBER}"
        DOCKER_HUB_CRED = "docker-hub-credentials"

        TARGET_HOST     = "i-058b0e16725fde7bb"
        TARGET_CRED_ID  = "ec2-new-be-key"
    }

    stages {
        stage('1. Checkout') {
            steps {
                checkout scm
            }
        }

        stage('2. Build Docker Image Backend') {
            steps {
                script {
                    sh "docker build --no-cache -t ${DOCKER_HUB_USER}/${IMAGE_NAME}:${IMAGE_TAG} ."
                }
            }
        }

        stage('3. Push Image lên Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: "${DOCKER_HUB_CRED}", passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh "echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin"
                        sh "docker push ${DOCKER_HUB_USER}/${IMAGE_NAME}:${IMAGE_TAG}"
                    }
                }
            }
        }

        stage('4. Deploy Cụm Compose sang EC2 BE Mới') {
            steps {
                script {
                    sshagent([ "${TARGET_CRED_ID}" ]) {
                        sh """
                        export PATH=/var/jenkins_home/aws-cli-bin:\$PATH

                        # 🛠️ SỬA 1: Ép tạo thư mục đích trước khi gửi file qua scp để tránh lỗi "No such file or directory"
                        ssh -o StrictHostKeyChecking=no \
                            -o ProxyCommand="aws ssm start-session --target %h --document-name AWS-StartSSHSession --parameters portNumber=%p" \
                            ubuntu@${TARGET_HOST} "mkdir -p /home/ubuntu/clyvasync-backend"

                        # 1. Đồng bộ các file docker-compose sang thư mục deploy trên EC2 BE mới
                        scp -o StrictHostKeyChecking=no \
                            -o ProxyCommand="aws ssm start-session --target %h --document-name AWS-StartSSHSession --parameters portNumber=%p" \
                            docker-compose.yml docker-compose.prod.yml ubuntu@${TARGET_HOST}:/home/ubuntu/clyvasync-backend/

                        # 2. SSH sang để thực thi khởi chạy cụm Compose
                        ssh -o StrictHostKeyChecking=no \
                            -o ProxyCommand="aws ssm start-session --target %h --document-name AWS-StartSSHSession --parameters portNumber=%p" \
                            ubuntu@${TARGET_HOST} '

                            cd /home/ubuntu/clyvasync-backend

                            # Cập nhật thông tin Image và Tag mới nhất vào file .env cố định của server
                            sed -i "/^IMAGE_NAME=/d" .env || true
                            sed -i "/^BUILD_NUMBER=/d" .env || true
                            echo "IMAGE_NAME=${DOCKER_HUB_USER}/${IMAGE_NAME}" >> .env
                            echo "BUILD_NUMBER=${IMAGE_TAG}" >> .env

                            # Kéo Image App mới về
                            sudo docker pull ${DOCKER_HUB_USER}/${IMAGE_NAME}:${IMAGE_TAG}

                            # 🛠️ SỬA 2: Thêm "sudo" vào trước docker compose để tránh lỗi kẹt quyền kiểm soát container hệ thống
                            sudo docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

                            # Dọn dẹp các ảnh cũ bị thừa
                            sudo docker image prune -f
                        '
                        """
                    }
                }
            }
        }

        stage('5. Dọn dẹp máy Jenkins') {
            steps {
                script {
                    sh "docker image prune -f"
                }
            }
        }
    }
}