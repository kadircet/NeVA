from django.shortcuts import render, redirect, get_object_or_404
from django.core.exceptions import ObjectDoesNotExist
from django.http import HttpResponse, JsonResponse
from django.views.decorators.http import require_http_methods, require_GET, require_POST
from .protos.backend_pb2_grpc import *
from .protos.backend_pb2 import *
from .protos.user_pb2 import * 
import base64
host = 'neva.0xdeffbeef.com'
port = '50051'
# create credentials
credentials = grpc.ssl_channel_credentials()

# create channel using ssl credentials
channel = grpc.secure_channel('{}:{}'.format(host, port), credentials)
stub = BackendStub(channel)
#ajax requests
@require_POST
def login_request(request):
    email = request.POST['email']
    password = request.POST['password']
    login_request = LoginRequest(email = email, password=password, authentication_type='DEFAULT')
    try:
        result = stub.Login(login_request)
        data = { 
            'message': "Login Successful!",
            'type': 'success',
            'is_success': True,
            'token': list(result.token)
        }
    except grpc.RpcError as e:
        data = {
            'message': e.details(),
            'is_success': False,
            'type': 'error'
        }
    return JsonResponse(data)

# Create your views here.
def index(request):
    return render(request, 'index.html', {})
