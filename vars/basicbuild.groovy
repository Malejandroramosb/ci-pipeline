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
            //Need to remove the prefix ClipMX, ej: ClipMX/service-sales-coupon/branch
            PROJECT_NAME = "${JOB_NAME.split('/|\\.')[2]}"
            DEPENDABOT_PROJECT = "${JOB_NAME.split('/')[1]}"

            IMAGE_NAME = sh(
                    script: "cat DockerImageName",
                    returnStdout: true
            ).trim()
            //Following variables needed for maker checker
            GIT_LOG = sh(
                    script: "echo \$(git log --oneline -n 1 HEAD)",
                    returnStdout: true
            )
            GIT_MERGE = sh(
                    script: "echo \$(git log --pretty=format:\"%H\" --merges -n 2 | awk 'FNR==2')",
                    returnStdout: true
            )
            NODE_OPTIONS='--max_old_space_size=4096'
        }

        stages {
            //stage('Dependabot Alerts') {
             //  when{
             //       expression {GIT_BRANCH == 'master' || GIT_BRANCH == 'main'}
             //   }
             //   steps {
            //        script {
            //            Dependabot.getInstance().run(this)
            //      }   
            //    }
            //  }

        stage('Credentials Scan'){
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    script {
                        CredentialsScan.getInstance().run(this)
                    }
                }
            }
        }

            stage('Maker Checker'){
                when{
                    expression {GIT_BRANCH == 'master' || GIT_BRANCH == 'main'}
                }
                steps {
                    script{
                        MakerChecker.getInstance().run(this)
                    }
                }
            }

           stage('Update ECR artifacts'){
               steps {
                  script {
                    ECRDB.getInstance().run(this, "tests/docker/docker-compose.yml")
                  }
               }
           }

           stage('Install Dependencies'){
                steps {
                    withNPM(npmrcConfig: 'clipmx-github-package'){
                        sh 'npm install'
                    }                    
                }

            }

            stage('Testing') {
                steps {
                    script {
                        def test = ""
                        if(runTest) {
                            test = "test"
                        }
                        sh "CI=true npm run $test"
                    }
                }
            }

            stage('SonarQube Analysis'){
                steps {
                    script {
                        SonarAudit.getInstance().run(this)
                    }
                }
            }

            stage('Quality Gate'){
                steps {
                    script {
                        QualityGate.getInstance().run(this)
                    }
                }            
            }

            stage('Deploy to Repo') {
                steps {
                    script {
                        ECRPush.getInstance().run(this)
                    }
                }
            }
        }
        
        post {
            always {
                deleteDir() /* clean up our workspace */
            }
            success {
                script {
                    BuildSlackSender.getInstance().sendSuccess(this);
                    //Add a link to start the deploy process into stage inside the summary page
                    if(GIT_BRANCH == 'master' || GIT_BRANCH == 'main') {
                            buildAddUrl(title: 'Deploy to STAGE', url: "/job/stage-ecs-app-deploy/buildWithParameters/?token=MqJnxoL3p&BRANCH=${GIT_BRANCH}&COMMIT=${GIT_COMMIT_SHORT}&PROJECT=${PROJECT_NAME}")
                        }
                    else {
                            if(env.GIT_BRANCH.contains('/')) {
                                GIT_BRANCH_STANDARD = "${env.GIT_BRANCH.replaceAll('/', '-')}"
                                buildAddUrl(title: 'Deploy to DEV', url: "/job/dev-ecs-app-deploy/buildWithParameters/?token=MqJnxoL3p&BRANCH=${GIT_BRANCH_STANDARD}&COMMIT=${GIT_COMMIT_SHORT}&PROJECT=${PROJECT_NAME}")
                            }
                            else{
                                 buildAddUrl(title: 'Deploy to DEV', url: "/job/dev-ecs-app-deploy/buildWithParameters/?token=MqJnxoL3p&BRANCH=${GIT_BRANCH}&COMMIT=${GIT_COMMIT_SHORT}&PROJECT=${PROJECT_NAME}")
                            }
                         }
                }
            }
            unstable {
                script {
                    BuildSlackSender.getInstance().sendUnstable(this);
                }
            }
            failure {
                script {
                    BuildSlackSender.getInstance().sendFailure(this);
                }
            }
        }
    }
}
