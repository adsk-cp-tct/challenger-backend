Handlebars.registerHelper('formatDate', function(date, size){
    return date ? date.substring(0, size || 10).replace('T', ' ').replace(/-/g, '/') : 'Unknown';
});