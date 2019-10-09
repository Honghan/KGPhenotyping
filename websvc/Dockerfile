FROM tiangolo/uwsgi-nginx-flask:python3.6

RUN apt-get update &&\
    apt-get -yq install openjdk-8-jdk-headless

RUN pip install flask-restful

COPY ./app /app 

