#!groovy
timestamps {
  node('docker') {
      slackJobDescription = "job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})"
      try {
          dockerRepoGolang = "test-golang-${env.BUILD_TAG}"
          dockerRepoOpenjdk = "test-openjdk-${env.BUILD_TAG}"
          dockerRepoClojure = "test-clojure-${env.BUILD_TAG}"
          stage("Build") {
              checkout scm

              parallel (
                golang: { sh "docker build --pull --no-cache --rm -f Dockerfile-golang -t ${dockerRepoGolang} ." },
                openjdk: { sh "docker build --pull --no-cache --rm -f Dockerfile-openjdk -t ${dockerRepoOpenjdk} ." },
                clojure: { sh "docker build --pull --no-cache --rm -f Dockerfile-clojure -t ${dockerRepoClojure} ." },
              )
          }

          dockerPusher = "push-${env.BUILD_TAG}"
          try {
              milestone 100
              stage("Docker Push") {
                  service = readProperties file: 'service.properties'

                  dockerPushRepoGolang = "${service.dockerUser}/golang-base:${env.BRANCH_NAME}"
                  dockerPushRepoOpenjdk = "${service.dockerUser}/openjdk-base:${env.BRANCH_NAME}"
                  dockerPushRepoClojure = "${service.dockerUser}/clojure-base:${env.BRANCH_NAME}"
                  lock("docker-push-baseimages") {
                    milestone 101
                    sh "docker tag ${dockerRepoGolang} ${dockerPushRepoGolang}"
                    sh "docker tag ${dockerRepoOpenjdk} ${dockerPushRepoOpenjdk}"
                    sh "docker tag ${dockerRepoClojure} ${dockerPushRepoClojure}"
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'jenkins-docker-credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME']]) {
                        sh """docker run -e DOCKER_USERNAME -e DOCKER_PASSWORD \\
                                         -v /var/run/docker.sock:/var/run/docker.sock \\
                                         --rm --name ${dockerPusher} \\
                                         docker:\$(docker version --format '{{ .Server.Version }}') \\
                                         sh -e -c \\
                              'docker login -u \"\$DOCKER_USERNAME\" -p \"\$DOCKER_PASSWORD\" && \\
                               docker push ${dockerPushRepoGolang} && \\
                               docker push ${dockerPushRepoOpenjdk} && \\
                               docker push ${dockerPushRepoClojure} && \\
                               docker logout'"""
                    }
                  }
              }
          } finally {
              sh returnStatus: true, script: "docker kill ${dockerPusher}"
              sh returnStatus: true, script: "docker rm ${dockerPusher}"

              sh returnStatus: true, script: "docker rmi ${dockerRepoGolang}"
              sh returnStatus: true, script: "docker rmi ${dockerRepoOpenjdk}"
              sh returnStatus: true, script: "docker rmi ${dockerRepoClojure}"

              sh returnStatus: true, script: "docker rmi \$(docker images -qf 'dangling=true')"
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
}
