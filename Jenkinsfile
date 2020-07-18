pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B clean compile'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/TEST-*.xml'
            gamekins jacocoCSVPath: '**/target/site/jacoco/jacoco.csv', jacocoResultsPath: '**/target/site/jacoco/'
        }
    }
}
