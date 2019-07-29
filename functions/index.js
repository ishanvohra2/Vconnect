//#Note - messageIds must be unique otherwise two notifications with same message Id will overlap

const functions = require('firebase-functions');

const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.postLike = functions.database.ref('/likes/{likerId}/{eventId}').onCreate((snapshot, context) => {

	//get the eventId
	const eventId = context.params.eventId;
	//get the hostId
	const likerId = context.params.likerId;

	console.log("eventId: " + eventId);
	console.log("likerId: " + likerId);


	//define the message id. We'll be sending this in the payload
	const messageId = "1";
		

	//get the description of the post and the userId of the receiver
	return admin.database().ref("events/" + eventId).once('value').then(snap => {
	const description = snap.child('description').val();	
	const receiverId = snap.child('hostId').val();

	console.log("receiverId: " + receiverId);

	//Don't send the notification if a user has liked his onw post
	if(receiverId !== likerId){

			//get the token of the user receiving the message and the dsiplayName of the user who like the post
			return admin.database().ref("/users").once('value').then(snap => { 
			var token = snap.child(receiverId).child('messagingToken').val();
			const displayName = snap.child(likerId).child('displayName').val();
			console.log("displayName: " + displayName);
			console.log("description: " + description)

			const title = displayName + " liked your post";
			shortMessage = title;
			longMessage = title;
			//get the message if description exists
			if(description != null){

				//get the first 10 characters of the description
				if(description.length > 20){
					shortDescription = description.substring(0,20);
					shortMessage = title + ' \"' + shortDescription +  "..." + '\"'; 
				}
				else{
					shortMessage = title + ' \"' + description + '\"';
					longMessage = shortMessage;
				}

				if(description.length > 80){
					longDescription = description.substring(0,80);
					longMessage = title + ' \"' + longDescription +  "..." + '\"'; 
				}
				else{
					longMessage = shortMessage;
				}
			}

			
			//we have everything we need
			//Build the message payload and send the message
			console.log("Construction the notification message.");
			const payload = {
				data: {
					data_type: "postLike",
					data_title: title,
					data_message_short: shortMessage,
					data_message_long: longMessage,
					data_message_id: messageId,
					data_link:eventId
				}
			};
			
			return admin.messaging().sendToDevice(token, payload)
						.then(function(response) {
							console.log("Successfully sent message:", response);

							//Save the notification in the database
							var notificationRef = admin.database().ref('users/' + receiverId + '/notifications');
							var newNotificationRef = notificationRef.push();

							newNotificationRef.set({
								notificationType: 'postLike',
								notification: longMessage,
								link: eventId //Link is either the id of the psot or the user id(in case of follow)
							}); 
						  })
						  .catch(function(error) {
							console.log("Error sending message:", error);
						  });
			});
		}
		else{
			return null;
		}

		});
});

exports.follow = functions.database.ref('/users/{userId}/followers/{followingId}').onCreate((snapshot, context) => {

	//get the userId
	const userId = context.params.userId;
	//get the followingId
	const followingId = context.params.followingId;

	console.log("userId: " + userId);
	console.log("followingId: " + followingId);


	//define the message id. We'll be sending this in the payload
	const messageId = "2";

			//get the token of the user receiving the message and the dsiplayName of the user who followed you
			return admin.database().ref("/users").once('value').then(snap => { 
			var token = snap.child(followingId).child('messagingToken').val();
			const displayName = snap.child(userId).child('displayName').val();
			console.log("displayName: " + displayName);

			const title = displayName + " followed you!";
			shortMessage = "";
			longMessage = "";			

			
			//we have everything we need
			//Build the message payload and send the message
			console.log("Construction the notification message.");
			const payload = {
				data: {
					data_type: "follow",
					data_title: title,
					data_message_short: shortMessage,
					data_message_long: longMessage,
					data_message_id: messageId,
					data_link: userId
				}
			};
			
			return admin.messaging().sendToDevice(token, payload)
						.then(function(response) {
							console.log("Successfully sent message:", response);

							//Save the notification in the database
							var notificationRef = admin.database().ref('users/' + followingId + '/notifications');
							var newNotificationRef = notificationRef.push();

							newNotificationRef.set({
								notificationType: 'follow',
								notification: title,
								link: userId //Link is either the id of the psot or the user id(in case of follow)
							}); 
						  })
						  .catch(function(error) {
							console.log("Error sending message:", error);
						  });
			});

});

exports.postComment = functions.database.ref('/events/{eventId}/comments/{commentId}').onCreate((snapshot, context) => {

	//get the eventId
	const eventId = context.params.eventId;
	//get the commentId
	const commentId = context.params.commentId;
	//get the userId of the commenter
	const commenterId = snapshot.child('userId').val();
	//get the comment
	const comment = snapshot.child('message').val();

	console.log("eventId: " + eventId);
	console.log("commentId: " + commentId);
	console.log("commenterId: " + commenterId);
	console.log("message: " + comment);


	//define the message id. We'll be sending this in the payload
	const messageId = "3";

		//get the userId of the user receiving the notification
		return admin.database().ref("/events/" + eventId + "/hostId").once('value').then(snap => { 
			const hostId = snap.val();

			//Don't send the notification if the user has commented on his own post
			if(hostId !== commenterId){

			//get the token of the user receiving the message and the dsiplayName of the user who commented on your post
			return admin.database().ref("/users").once('value').then(snap => { 
			var token = snap.child(hostId).child('messagingToken').val();
			const displayName = snap.child(commenterId).child('displayName').val();
			console.log("displayName: " + displayName);

			const title = displayName + " commented on your post";
			shortMessage = displayName + " wrote: ";
			longMessage = shortMessage;

				//get the first 20 characters of the comment
				if(comment.length > 20){
					shortComment = comment.substring(0,20);
					shortMessage = shortMessage + ' \"' + shortComment +  "..." + '\"'; 
					longMessage = longMessage + ' \"' + comment + '\"'; 
				}
				else{
					shortMessage = shortMessage + ' \"' + comment + '\"';
					longMessage = shortMessage;
				}
							

			
			//we have everything we need
			//Build the message payload and send the message
			console.log("Construction the notification message.");
			const payload = {
				data: {
					data_type: "postComment",
					data_title: title,
					data_message_short: shortMessage,
					data_message_long: longMessage,
					data_message_id: messageId,
					data_link: eventId
				}
			};
			
			return admin.messaging().sendToDevice(token, payload)
						.then(function(response) {
							console.log("Successfully sent message:", response);

							//Save the notification in the database
							var notificationRef = admin.database().ref('users/' + hostId + '/notifications');
							var newNotificationRef = notificationRef.push();

							newNotificationRef.set({
								notificationType: 'postComment',
								notification: longMessage,
								link: eventId //Link is either the id of the psot or the user id(in case of follow)
							}); 
						  })
						  .catch(function(error) {
							console.log("Error sending message:", error);
						  });
			});
		}
			else{
				return null;
			}

		});

});