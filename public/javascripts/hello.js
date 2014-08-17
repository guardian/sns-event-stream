(function () {
    var eventStream = document.getElementById("event-stream");

    function appendMessage(message) {
        var p = document.createElement("p");
        var text = document.createTextNode(message);
        p.appendChild(text);
        eventStream.appendChild(p);
    }

    var feed = new EventSource('/events');

    feed.addEventListener('message', function (message) {
        appendMessage(message.message)
    }, false);
})();