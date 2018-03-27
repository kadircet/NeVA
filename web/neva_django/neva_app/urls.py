from django.urls import path

from . import views

urlpatterns = [
    path('', views.index, name='index'),
    path('ajax/login/',views.login_request, name='login')
]
