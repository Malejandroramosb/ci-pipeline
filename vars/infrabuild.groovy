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
        
        }

        stages {

           stage('Run CF linter'){
               steps {
                  script {
                    sh 'sudo /home/ec2-user/.local/bin/cfn-lint ecs.yaml'
                  }
               }
           }
           stage('Create Stack'){
               steps {
                  script {
                    sh 'aws cloudformation deploy --template-file ecs.yaml --stack My-New-ECS'
                  }
               }
           }
        
        }
    }
}
