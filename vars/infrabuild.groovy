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
                    //sh 'sudo /home/ec2-user/.local/bin/cfn-lint ecs.yaml'
                    echo "not doing anything"
                  }
               }
           }
           stage('Create Stack'){
               steps {
                  script {
                    sh 'aws cloudformation deploy --template-file ecs.yaml --stack My-New-ECS --region us-east-1 --parameter-overrides KeyName=test VpcId=vpc-0783368ea2d95fb42 SubnetId=subnet-03dd89fed4d13aa64,subnet-050a9bb98a0c4cab0 --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND'
                  }
               }
           }
        
        }
    }
}
