$(document).ready(function() {
  $(document).on('click', '#login_button', login);
});
function login() {
  $.ajaxSetup({
    beforeSend: function(xhr, settings) {
      xhr.setRequestHeader('X-CSRFToken', getCookie('csrftoken'));
    }
  });
  var url = 'ajax/login/';
  var data = $('#login_form').serializeJSON();
  var dataType = 'json';
  var type = 'POST';
  var success = function(response, statusTxt, xhr) {
    $.notify(
        response.message, {autoHideDelay: 20000, className: response.type});
  };
  $.ajax(
      {url: url, data: data, dataType: dataType, type: type, success: success});
}


function getCookie(name) {
  var cookieValue = null;
  if (document.cookie && document.cookie != '') {
    var cookies = document.cookie.split(';');
    for (var i = 0; i < cookies.length; i++) {
      var cookie = cookies[i].trim();
      // Does this cookie string begin with the name we want?
      if (cookie.substring(0, name.length + 1) == (name + '=')) {
        cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
        break;
      }
    }
  }
  return cookieValue;
}
