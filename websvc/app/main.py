
# REST service to handle ODBA requests
#

from flask import Flask, request, jsonify
from flask_restful import Api, Resource, reqparse
import json
import subprocess
import tempfile
from os import unlink
import sys

app = Flask(__name__)
api = Api(app)

_classpath = "/app/OntologyPhenotyping-0.0.1-SNAPSHOT.jar:/app/lib/*"
_class = "ac.uk.hdruk.graph.CLIQuery"


def wrap_jsonp(callback, r):
    print('callback : [%s]' % callback, file=sys.stdout)
    if callback:
        return app.response_class(
            response = callback + '(' + r + ')',
            status=200,
            mimetype='application/json'
        )
    else:
        return r, 200


class StudyList(Resource):

    # list existing studies
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument("callback")
        message = request.data
        args = parser.parse_args()
        result = subprocess.run(["java", "-cp", _classpath, _class, "-l"], \
            stdout=subprocess.PIPE, encoding='utf-8')
        if not result.returncode == 0:
             return "Command exited with non-zero return code", 500
        return wrap_jsonp(args["callback"], result.stdout)


class Study(Resource):
    # create/update a study
    def get(self, name):
        parser = reqparse.RequestParser()
        parser.add_argument("rules")
        parser.add_argument("callback")

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

        return wrap_jsonp(args["callback"], json.dumps(output))


class Query(Resource):

    # run a query
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument("id")
        parser.add_argument("study")
        parser.add_argument("sparql")
        parser.add_argument("callback");

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

        return wrap_jsonp(args["callback"], json.dumps(output))

    def post(self):
        return '', 405

api.add_resource(Query, "/query")
api.add_resource(StudyList, "/liststudy")
api.add_resource(Study, "/study/<string:name>")

if (__name__ == '__main__'):
    app.run(debug=True, port=5000)

