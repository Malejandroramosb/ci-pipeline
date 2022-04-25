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
                    sh 'docker build .'
                  }
               }
           }


            stage('Push to Docker Registry'){
                steps {
                    script {
                        echo "not doing anything"
                    }
                }
            }

        
        }
    }
}
