pipeline {
    agent any
    tools {
        maven "Maven 3"
        jdk "Java 8"
    }
    stages {
        stage ("Initialize") {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }
        stage ("Build") {
            steps {
                sh "mvn clean deploy"
            }
            post {
                success {
                  	archiveArtifacts artifacts: "pom.xml"
                }
            }
        }
    }
}

