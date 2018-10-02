'use strict';
const socket = new WebSocket('ws://' + window.location.host + '/signal');
const peerConnectionConfig = {
    'iceServers': [
        {'urls': 'stun:stun.stunprotocol.org:3478'},
        {'urls': 'stun:stun.l.google.com:19302'},
    ]
};

const videoButtonOff = document.querySelector('#video_off');
const videoButtonOn = document.querySelector('#video_on');
const audioButtonOff = document.querySelector('#audio_off');
const audioButtonOn = document.querySelector('#audio_on');
const exitButton = document.querySelector('#exit');
const localVideo = document.querySelector('#local_video');
const remoteVideo = document.querySelector('#remote_video');
const localUser = localStorage.getItem("uuid");
let localStream;
let myPeerConnection;

const mediaConstraints = {
    audio: true,
    video: true
};

// run when page loads
$(function(){
    start();
});

// add an event listener to get to know when a connection is open
socket.onopen = function() {
    console.log('WebSocket connection opened. Ready to send messages');
    // send a message to the server
    sendToServer({
        from: localUser,
        type: 'text',
        data: 'Client '+ localUser +' connected'
    });
};

// add an event listener for a message being received
socket.onmessage = function(message) {
    const webSocketMessage = JSON.parse(message.data);
    if (webSocketMessage.type === 'text') {
        console.log('Text message from ' + webSocketMessage.from + ' received: ' + webSocketMessage.data);
    } else if (webSocketMessage.type === 'signal') {
        console.log('Signal received from server');

    } else {
        console.log('Error: Join type message received from server');
    }
};

// a listener for the socket being closed event
socket.onclose = function(message) {
    console.log('Socket has been closed');
};

// an event listener to handle errors
socket.onerror = function(message) {
    console.log("Error: " + message);
};

// use JSON format to send WebSocket message
function sendToServer(msg) {
    let msgJSON = JSON.stringify(msg);
    socket.send(msgJSON);
}

// mute video
videoButtonOff.onclick = () => {
    mediaConstraints.video = false;
    getMedia(mediaConstraints);
    console.log('Video Off');
};
videoButtonOn.onclick = () => {
    mediaConstraints.video = true;
    getMedia(mediaConstraints);
    console.log('Video On');
};

// mute audio
audioButtonOff.onclick = () => {
    mediaConstraints.audio = false;
    getMedia(mediaConstraints);
    console.log('Audio Off');
};
audioButtonOn.onclick = () => {
    mediaConstraints.audio = true;
    getMedia(mediaConstraints);
    console.log('Audio On');
};

// close socket when exit
exitButton.onclick = () => {
    stop();
};

// create peer connection, init local stream
function start() {
    createPeerConnection();
    getMedia(mediaConstraints);
}

function stop() {
    // send a message to the server
    sendToServer({
        from: localUser,
        type: 'text',
        data: 'Client ' + localUser +' disconnected'
    });
    socket.close();
    //TODO close all
}

// initialize media stream
function getMedia(constraints) {
    if (localStream) {
        localStream.getTracks().forEach(track => {track.stop();});
    }
    navigator.mediaDevices.getUserMedia(constraints)
        .then(getLocalMediaStream).catch(handleGetUserMediaError);
}

// handle get media error
function handleGetUserMediaError(error) {
    console.log('navigator.getUserMedia error: ', error);
    switch(error.name) {
        case "NotFoundError":
            alert("Unable to open your call because no camera and/or microphone" +
                "were found.");
            break;
        case "SecurityError":
        case "PermissionDeniedError":
            // Do nothing; this is the same as the user canceling the call.
            break;
        default:
            alert("Error opening your camera and/or microphone: " + error.message);
            break;
    }

    stop();
}

// add MediaStream to local video element and to the Peer
function getLocalMediaStream(mediaStream) {
    localStream = mediaStream;
    localVideo.srcObject = mediaStream;
    localStream.getTracks().forEach(track => myPeerConnection.addTrack(track, localStream));
}

function createPeerConnection() {
    myPeerConnection = new RTCPeerConnection(peerConnectionConfig);

    // myPeerConnection.onicecandidate = handleICECandidateEvent;
    // myPeerConnection.ontrack = handleTrackEvent;
    // myPeerConnection.onnegotiationneeded = handleNegotiationNeededEvent;
    // myPeerConnection.onremovetrack = handleRemoveTrackEvent;
    // myPeerConnection.oniceconnectionstatechange = handleICEConnectionStateChangeEvent;
    // myPeerConnection.onicegatheringstatechange = handleICEGatheringStateChangeEvent;
    // myPeerConnection.onsignalingstatechange = handleSignalingStateChangeEvent;
}


