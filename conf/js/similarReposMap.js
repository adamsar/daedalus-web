//Takes in a list of repos for the the language
//that similar repos are to be found for.
function () {
    emit(this._id, {
        name: this.name,
        relatedEntities: this.relatedEntities
    });
}