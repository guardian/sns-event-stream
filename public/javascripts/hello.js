(function () {
    console.log("Hi");

    var feed = new EventSource('/events');

    feed.addEventListener('message', function (message) {
        console.log(message);
    }, false);
})();