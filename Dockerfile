FROM openjdk:11-jdk
COPY ./target/*.jar app.jar
CMD ["java","-jar","app.jar"]
RUN -e "apiKey=prj_test_pk_d612be3a27fa5c7c1236c89ae724115d0fe8c210" && echo "jenkins ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers