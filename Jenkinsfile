pipeline {
    agent any

    environment {
        // Thông tin máy EC2 Target (Nơi chạy App)
        TARGET_USER = "ubuntu"
        TARGET_IP   = "3.236.101.255"
        TARGET_DIR  = "/home/ubuntu/app/booking"
        SSH_KEY_ID  = "ec2-server-key" // ID SSH Key bạn lưu trong Jenkins Credentials
    }


    stages {
        stage('Checkout') {
            steps {
                // Jenkins kéo code từ Git về máy Jenkins
                checkout scm
            }
        }

        stage('Sync to Target') {
            steps {
                sshagent([env.SSH_KEY_ID]) {
                    sh """
                        # Tạo thư mục trên Target nếu chưa có
                        ssh -o StrictHostKeyChecking=no ${TARGET_USER}@${TARGET_IP} "mkdir -p ${TARGET_DIR}"

                        # Đẩy toàn bộ code sang máy Target (bao gồm cả 3 file docker-compose)
                        scp -r -o StrictHostKeyChecking=no ./* ${TARGET_USER}@${TARGET_IP}:${TARGET_DIR}
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                sshagent([env.SSH_KEY_ID]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${TARGET_USER}@${TARGET_IP} "
                            cd ${TARGET_DIR} && \
                            # Chạy lệnh kết hợp cả 3 file compose theo đúng thứ tự
                            docker compose -f docker-compose.yml \
                                           -f docker-compose.prod.yml \
                                           -f docker-compose.override.yml \
                                           up --build -d && \
                            # Dọn dẹp tài nguyên thừa
                            docker image prune -f
                        "
                    """
                }
            }
        }
    }
}