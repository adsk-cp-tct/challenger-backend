function displayEvent(event, img) {
    $.get('/assets/templates/admin/event.create.tmpl.html', function(data) {
        var template = Handlebars.compile(data);
        $(".events-table").append(template({event: event, img: img}));
    });
}

function showDetails(sender) {
    window.open('/admin/events/' + $(sender).text());
}

function displayIdea(ides, img) {
    $(".ideas-table").append(
         '<tr>'
        +'    <td>' + idea.id + '</td>'
        +'    <td><img height="80" src=' + img + '/></td>'
        +'    <td>' + idea.title + '</td>'
        +'    <td>' + idea.description + '</td>'
        +'    <td>' + idea.likedUsers.length + '</td>'
        +'    <td>' + idea.followers.length + '</td>'
        +'    <td><a id="' + idea.id + '" onclick="unpublishIdea();" href="">Unpublish</a></td>'
        +'</tr>'
    );
}

function revokeEvent(sender) {
    var id = sender.id;
    $.ajax({
        type: 'DELETE',
        url: '/admin/events/' + id,
        async: false,
        success: function(data) {
            alert("Event revoked: " + id);
            $('#event-row-' + id).remove();
        },
        error: function(jqxhr) {
            alert('Revoking event failed due to ' + jqxhr.getResponseHeader('x-ads-troubleshooting'));
        }
    });
}

function getThumbnail(imageName, successCallback, errorCallback){
    $.ajax({
        type: 'GET',
        url: '/thumbnail/' + imageName + '/300x200',
        async: false,
        dataType: "text",
        success: function (data) {
            successCallback(data);
        },
        error: function (jqxhr) {
            if (jqxhr.status == "404") {
                errorCallback(defaultImg);
            } else {
                alert(jqxhr.responseText);
            }
        }
    });
}

function resetCreateEventUI() {
    $(".title-input").val('');
    $(".due-input").val('');
    $(".start").val('08:30');
    $(".end").val('09:30');
    $(".max-input").val('');
    $(".location-input").val('');
    $(".presenter-input").val('');
    $("#presenterLogoPath").attr('data-path', '').hide();
    $(".presenter-email-input").val('');
    $(".presenter-title-input").val('');
    $(".des-input").val('');
    $("#uploadPath").attr('data-path', '').hide();
    $("#uploadPresenterLogo")[0].reset();
    $("#uploadForm")[0].reset();
    $("#logo-tip").text('Choose an image..');
    $("#event-img-tip").text('Choose an image..');
}

// For creating table
$(function() {
    $('#create-event-dialog').on('show', function () {
      resetCreateEventUI();
    })

    $('#btn-publish-event').click(function (e) {
        var schedule = $(".schedule-input").val(), newEvent = {
            title: $(".title-input").val(),
            category: $(".type-input").val(),
            expiration: $(".due-input").val(),
            startTime: schedule + 'T' + $(".start").val(),
            endTime: schedule + 'T' + $(".end").val(),
            seats: $(".max-input").val(),
            location: $(".location-input").val(),
            presenter: $(".presenter-input").val(),
            presenterLogo: $("#presenterLogoPath").attr('data-path').trim(),
            presenterEmail: $(".presenter-email-input").val(),
            presenterTitle: $(".presenter-title-input").val(),
            registerPolicy: $(".policy-select").val(),
            description: $(".des-input").val(),
            thumbnail: $("#uploadPath").attr('data-path').trim()
        };

        $.ajax({
            type: 'POST',
            url: '/events',
            dataType: 'json',
            async: false,
            data: JSON.stringify(newEvent),
            success: function(data) {
                data.applyingUserCount = 0;
                getThumbnail(
                    data.thumbnail, function(imgPath){
                        var img = "http://" + window.location.host + "/images/" + imgPath;
                        displayEvent(data, img?img:defaultImg)
                    }, function(img){
                        displayEvent(data, img)
                    });
                $('#create-event-dialog').modal('hide')
            },
            error: function(jqxhr) {
                alert(jqxhr.responseText)
            }
        });

    });

    $( "#uploadForm" ).submit(function( event ) {
        // Stop form from submitting normally
        event.preventDefault();

        $.ajax
        ({
            type: 'POST',
            url: '/image',
            processData: false,
            contentType: false,
            async: false,
            data: new FormData( this ),
            success: function(data) {
                $("#uploadPath").attr('data-path', data).show();
            },
            error: function(jqxhr) {
                alert(jqxhr.responseText)
            }
        })
    });

    $( "#uploadPresenterLogo" ).submit(function( event ) {
        // Stop form from submitting normally
        event.preventDefault();

        $.ajax({
            type: 'POST',
            url: '/image',
            processData: false,
            contentType: false,
            async: false,
            data: new FormData( this ),
            success: function(data) {
                $("#presenterLogoPath").attr('data-path', data).show();
            },
            error: function(jqxhr) {
                alert(jqxhr.responseText);
            }
        })
    });
});
