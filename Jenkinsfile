pipeline {
        agent any
    
    options {
        timeout(time: 20, unit: 'MINUTES') 
    }

    environment {
        max = 50
        random_num = "${Math.abs(new Random().nextInt(max+1))}"
    }
    

    stages {
        stage('Create docker'){
            steps {
                sh 'docker build . -t ${env.random_num}'
                
            }
        }

    

    }
}
