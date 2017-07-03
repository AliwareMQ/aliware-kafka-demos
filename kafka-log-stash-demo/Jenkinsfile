pipeline {
    agent any

    stages {
        stage('Build') {
            when {
                branch 'master'
            }
            steps {
                sh "ls"
            }
        }

        stage('Deploy') {
            when {
                branch 'master'
            }
            steps {
                sh "docker build -t $IMAGE_NAME ."
                sh "docker login -u $DOCKER_USER -p $DOCKER_PASS $REGISTRY_URL"
                sh "docker push $IMAGE_NAME:latest"
                echo 'Deloying....'
            }
        }

    }
}
