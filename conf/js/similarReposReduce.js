function (key, values) {

    var matches = 0;
    values[0].relatedEntities.forEach(function(related){
        if(repos.indexOf(related.name) > -1) {
            matches++;
        }
    });

    return {
        repoId: key,
        name: values[0].name,
        matches: matches
    };
}