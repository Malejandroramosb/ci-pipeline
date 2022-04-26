def call() {
    

    pipeline {
        agent any

        
        tools {
            nodejs 14
        }

        environment {
            GIT_COMMIT_SHORT = sh(
                    script: "printf \$(git rev-parse --short ${GIT_COMMIT})",
                    returnStdout: true
            )

            IMAGE_NAME = sh(
                    script: "cat DockerImageName",
                    returnStdout: true
            )
        }

        stages {

           stage('Create Docker image'){
               steps {
                  script {
                    sh 'docker build -t $IMAGE_NAME:$GIT_COMMIT_SHORT .'
                  }
               }
           }
           stage('Push to Docker registry'){
               steps {
                  script {
                    sh '''aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 040877022702.dkr.ecr.us-east-1.amazonaws.com
                    docker push $IMAGE_NAME:$GIT_COMMIT_SHORT'''
                  }
               }
           }
        
        }
    }
}
