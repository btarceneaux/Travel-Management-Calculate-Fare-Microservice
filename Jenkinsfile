pipeline {
    agent any 

    triggers {
        pollSCM('* * * * *')
    }
    // Got permission denied while trying to connect to the Docker daemon socket at unix.
    // sudo usermod -a -G docker jenkins
    // restart jenkins server ->  sudo service jenkins restart

    environment
    {
        apiKey = 'prj_test_pk_d612be3a27fa5c7c1236c89ae724115d0fe8c210'
    }

    stages {
            
        stage('Maven Compile') {
            steps {
                echo '----------------- Compiling project ----------'
                sh 'mvn clean compile'
            }
        }
        
         stage('Maven Test') {
            steps {
                echo '----------------- Testing project ----------'
                sh 'mvn clean test'
            }
        }
        
        stage('Maven Build') {
             steps {
                echo '----------------- Building project ----------'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                echo '----------------- Building docker image ----------'
                sh '''
                    docker image build -t calculating-service .
                '''
            }
        }

        stage('Docker Deploy') {
            steps {
                echo '----------------- Deploying docker image ----------'
                echo $apiKey
                sh '''
                 (if  [ $(docker ps -a | grep calculating-service | cut -d " " -f1) ]; then \
                        echo $(docker rm -f calculating-service); \
                        echo "---------------- successfully removed calculating-service ----------------"
                     else \
                    echo OK; \
                 fi;);
            docker container run \
            --env apiKEY=$apiKey \
            --restart always \
            --name calculating-service \
            -p 8082:8082 \
            -d calculating-service && \
            docker network connect travel-management-network calculating-service
            '''
            }
        }
    }
}