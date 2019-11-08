
# REST service to handle ODBA requests
#

from flask import Flask, request
from flask_restful import Api, Resource, reqparse
import json
import subprocess
import tempfile
from os import unlink

app = Flask(__name__)
api = Api(app)

_classpath = "/app/OntologyPhenotyping-0.0.1-SNAPSHOT.jar:/app/lib/*"
_class = "ac.uk.hdruk.graph.CLIQuery"

class StudyList(Resource):

    # list existing studies
    def get(self):
        result = subprocess.run(["java", "-cp", _classpath, _class, "-l"], \
            stdout=subprocess.PIPE, encoding='utf-8')
        if not result.returncode == 0:
             return "Command exited with non-zero return code", 500
        return result.stdout, 200

class Study(Resource):

    # get an exisitng study
    def get(self, name):
        result = subprocess.run(["java", "-cp", _classpath, _class, "-l", name], \
            stdout=subprocess.PIPE, encoding='utf-8')
        if result.stdout.find("NOT_FOUND") == 0:
             return "Study not found", 404
        if not result.returncode == 0:
             return "Command exited with non-zero return code", 500
        return result.stdout, 200

    # create/update a study
    def put(self, name):
        parser = reqparse.RequestParser()
        parser.add_argument("rules")

        message = request.data
        args = parser.parse_args()

        if not args["rules"]:
            args["rules"] = " "

        tf = tempfile.NamedTemporaryFile(mode='w', delete=False)
        tf.write(args["rules"])
        tf.close()
        result = subprocess.run(["java", "-cp", _classpath, _class, name, "-r", tf.name], \
                stdout=subprocess.PIPE, encoding='utf-8')
        unlink(tf.name)

        if not result.returncode == 0:
             return "Command exited with non-zero return code", 500

        output = { "study": name, "result": result.stdout }
        status = 200

        return output, status


class Query(Resource):

    # run a query
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument("id")
        parser.add_argument("study")
        parser.add_argument("sparql")

        message = request.data
        args = parser.parse_args()

        if not args["id"]:
            return 'Missing query ID', 400
        if not args["sparql"]:
            return 'Missing query SPARQL', 400
        if not args["study"]:
            return 'Missing study', 400

        tf = tempfile.NamedTemporaryFile(mode='w', delete=False)
        tf.write(args["sparql"])
        tf.close()
        result = subprocess.run(["java", "-cp", _classpath, _class, args["study"], "-q", tf.name], \
                stdout=subprocess.PIPE, encoding='utf-8')
        unlink(tf.name)

        if not result.returncode == 0:
             return "Command exited with non-zero return code", 500

        output = { "id": args["id"], "result": result.stdout }
        status = 200

        return output, status

    def get(self):
        return '', 405

api.add_resource(Query, "/query")
api.add_resource(StudyList, "/study")
api.add_resource(Study, "/study/<string:name>")

if (__name__ == '__main__'):
    app.run(debug=True, port=5000)

