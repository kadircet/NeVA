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

  $('.updateMealName').on('click', function() {
    var table_id = $(this).attr('table_id');
    var meal_name = $('#meal_name'+table_id).val();
    req = sendPostReqUpdateMeal(table_id, meal_name);
    req.done(function(data) {
      if (data.result == 'success') {
        $('#meal_name'+table_id).fadeOut(200).fadeIn(200);
      }
    })
  })

  $('.addNewMeal').on('click', function() {
    var meal_name = $('#new_meal_name').val();
    
    if(meal_name != '') {
      req = sendPostReqAddMeal(meal_name);
      req.done(function(data) {
        if(data.result == 'success') {
          document.getElementById("new_meal_name").reset();
          $('#new_meal_name').fadeOut(200);
          $('#new_meal_name').fadeIn(200);
          $('#new_meal_name').val('');      
        }
      })
      
    }
  });

  $('.addNewTag').on('click', function() {
    var tag_name = $('#new_tag_name').val();
    $('#new_tag_name').val('');
    if(tag_name != '') {
      req = sendPostReqAddTag(tag_name);
      req.done(function(data) {
        if(data.result == 'success') {
          $('#new_tag_name').fadeOut(200);
          $('#new_tag_name').fadeIn(200);
          $('#new_tag_name').val('');
        }
      })
      
    }
  });

  $('.addNewTVS').on('click', function() {
    var meal_name = $('#new_tvs_meal').val();
    var tag_name = $('#new_tvs_tag').val();
    if(meal_name!='' && tag_name!='') {
      req = sendPostReqAddTVS(meal_name, tag_name);
      req.done(function(data) {
        if(data.result == 'success') {
          $('#new_tvs_meal').fadeOut(200);
          $('#new_tvs_tag').fadeOut(200);
          $('#new_tvs_meal').fadeIn(200);
          $('#new_tvs_tag').fadeIn(200);
          $('#new_tvs_meal').val('');
          $('#new_tvs_tag').val('');
        }
      })
    }
  })


});

function sendPostReqAddMeal(meal_name) {
  req = $.ajax({
    url: 'processAdd',
    type: 'POST',
    data: {name: meal_name, action: 'meal'}
  });
  return req;
}

function sendPostReqAddTag(tag_name) {
  req = $.ajax({
    url: 'processAdd',
    type: 'POST',
    data: {name: tag_name, action: 'tag'}
  });
  return req;
}

function sendPostReqAddTVS(meal_name, tag_name) {
  req = $.ajax({
    url: 'processAdd',
    type: 'POST',
    data: {meal_name: meal_name, tag_name: tag_name, action:'tvs'}
  });
  return req;
}

function sendPostReqUpdateMeal(table_id, meal_name) {
  req = $.ajax({
    url: 'processDbUpdate',
    type: 'POST',
    data: {id: table_id, name: meal_name}
  });
  return req;
}

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
