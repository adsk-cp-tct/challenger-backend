(function($, undefined) {
    var eventId = $('#event-details-container').attr('data-id');

    function renderUsers(template, users, category) {
        var promises = [], profiles = [];
        $.each(users, function (i, value) {
            promises.push($.get('/users/' + ((typeof i === 'number') ? value : i) + '/profile', function(data) {
                profiles.push(data);
            }));
        });

        $.when.apply($, promises).done(function() {
            $('#event-details-container').find('aside').append(template({
                title: category,
                profiles: profiles
            }));
        });
    }

    $.get('/events/' + eventId, function(json) {
        $.get('/assets/templates/admin/event.details.tmpl.html', function(data) {
            var template = Handlebars.compile(data);
            $('#event-details-container').find('main').html(template(json));
        });

        $.get('/assets/templates/admin/event.users.tmpl.html', function(data) {
            var template = Handlebars.compile(data);
            renderUsers(template, json.applyingUsers, 'Applying Users');
            renderUsers(template, json.registerUsers, 'Registered Users');
            renderUsers(template, json.followers, 'Following Users');
        });
    });
}(jQuery));