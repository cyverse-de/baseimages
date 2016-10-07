#!groovy
node('docker') {
    slackJobDescription = "job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})"
    try {
        stage "Build" {
            checkout scm

            service = readProperties file: 'service.properties'

            dockerRepoGolang = "test-golang-${env.BUILD_TAG}"

            sh "docker build --pull --no-cache --rm -f Dockerfile-golang -t ${dockerRepoGolang} ."
        }

        dockerPusher = "push-${env.BUILD_TAG}"
        try {
            stage "Docker Push" {
                dockerPushRepoGolang = "${service.dockerUser}/golang-base:${env.BRANCH_NAME}"
                sh "docker tag ${dockerRepoGolang} ${dockerPushRepoGolang}"
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'jenkins-docker-credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME']]) {
                    sh """docker run -e DOCKER_USERNAME -e DOCKER_PASSWORD \\
                                     -v /var/run/docker.sock:/var/run/docker.sock \\
                                     --rm --name ${dockerPusher} \\
                                     docker:\$(docker version --format '{{ .Server.Version }}') \\
                                     sh -e -c \\
                          'docker login -u \"\$DOCKER_USERNAME\" -p \"\$DOCKER_PASSWORD\" && \\
                           docker push ${dockerPushRepoGolang} && \\
                           docker logout'"""
                }
            }
        } finally {
            sh returnStatus: true, script: "docker kill ${dockerPusher}"
            sh returnStatus: true, script: "docker rm ${dockerPusher}"

            sh returnStatus: true, script: "docker rmi ${dockerRepoGolang}"
        }
    } catch (InterruptedException e) {
        currentBuild.result = "ABORTED"
        slackSend color: 'warning', message: "ABORTED: ${slackJobDescription}"
        throw e
    } catch (e) {
        currentBuild.result = "FAILED"
        sh "echo ${e}"
        slackSend color: 'danger', message: "FAILED: ${slackJobDescription}"
        throw e
    }
}
