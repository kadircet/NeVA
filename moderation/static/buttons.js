$(document).ready(function() {

  $('.acceptMeal').on('click', function() {
    var table_id = $(this).attr('table_id');
    var meal_name = $('#meal_name' + table_id).val();
    var action = 'acceptMeal';
    req = sendPostReq(table_id, meal_name, action);
    req.done(function(data) {
      if (data.result == 'success') {
        $('#meal_row_id' + table_id).fadeOut(200);
      }
    })
  });

  $('.rejectMeal').on('click', function() {
    var table_id = $(this).attr('table_id');
    var meal_name = $('#meal_name' + table_id).val();
    var action = 'rejectMeal';
    req = sendPostReq(table_id, meal_name, action);
    req.done(function(data) {
      if (data.result == 'success') {
        $('#meal_row_id' + table_id).fadeOut(200);
      }
    })
  });

  $('.acceptTag').on('click', function() {
    var table_id = $(this).attr('table_id');
    var tag_name = $('#tag_name' + table_id).val();
    var action = 'acceptTag';

    req = sendPostReq(table_id, tag_name, action);
    req.done(function(data) {
      if (data.result == 'success') {
        $('#tag_row_id' + table_id).fadeOut(200);
      }
    })

  });

  $('.rejectTag').on('click', function() {
    var table_id = $(this).attr('table_id');
    var tag_name = $('#tag_name' + table_id).val();
    var action = 'rejectTag';
    req = sendPostReq(table_id, tag_name, action);
    req.done(function(data) {
      if (data.result == 'success') {
        $('#tag_row_id' + table_id).fadeOut(200);
      }
    })
  });

  $('.acceptTagValue').on('click', function() {
    var table_id = $(this).attr('table_id');
    var meal_name = $('#tag_value_suggestee_name' + table_id).val();
    var meal_id = $('#tag_value_suggestee_name' + table_id).attr('db_id');

    var tag_name = $('#tag_value_tag_name' + table_id).val();
    var tag_id = $('#tag_value_tag_name' + table_id).attr('db_id');
    var action = 'acceptTagValue';
    req = sendPostReqTagValue(table_id, meal_id, tag_id, action);
    req.done(function(data) {
      if (data.result == 'success') {
        $('#tag_value_row_id' + table_id).fadeOut(200);
      }
    })
  });

  $('.rejectTagValue').on('click', function() {
    var table_id = $(this).attr('table_id');
    var meal_id = $('#tag_value_suggestee_name' + table_id).attr('db_id');
    var tag_id = $('#tag_value_tag_name' + table_id).attr('db_id');
    var action = 'rejectTagValue';
    req = sendPostReqTagValue(table_id, meal_id, tag_id, action);
    req.done(function(data) {
      if (data.result == 'success') {
        $('#tag_value_row_id' + table_id).fadeOut(200);
      }
    })
  });


});


function sendPostReq(table_id, name, act) {
  req = $.ajax({
    url: 'process',
    type: 'POST',
    data: {id: table_id, suggestion: name, action: act}
  });
  return req;
}

function sendPostReqTagValue(table_id, meal_id, tag_id, action) {
  req = $.ajax({
    url: 'processTagValue',
    type: 'POST',
    data: {id: table_id, meal_id: meal_id, tag_id: tag_id, action: action}
  });
  return req;
}
