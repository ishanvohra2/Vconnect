# Documentation

Below are some code snippets for different functions in Vconnect

## Creating posts

Creating a post such as an article or an event will use the Event class. We will set the type to "article" or "event" depending on the usecase.
Here is the code for posting an article : 


```bash
    private void addPost() throws ParseException {
        String articleTxt = articleText.getText().toString();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        if (!TextUtils.isEmpty(articleTxt)) {

            String id = databaseReference.push().getKey();
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            String date = day + "/" + month + "/" + year;

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String imgPath = uploadImage(id);

            final Event event = new Event();
            event.setType("article");
            event.setPublishDate(date);
            if (!TextUtils.isEmpty(imgPath))
                event.setImgUrl(imgPath);
            event.setHostId(userId);
            event.setEventName("");
            event.setDescription(articleTxt);
            event.setPoints(5);

            databaseReference.child("events").child(id).setValue(event);
            databaseReference.child("users").child(event.getHostId()).child("points").setValue(userPoints + 5);
            Toast.makeText(getContext(), "5 Points Rewarded!", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(getContext(), "You should enter some text first", Toast.LENGTH_LONG).show();
        }
    }
```

## Creating Groups 
Groups can be created using Group class. The data members are assigned and then pushed into the database. 

```bash

final Group group = new Group();
                group.setAdminId(HomeActivity.userId);
                group.setGroupDescription(descriptionEt.getText().toString());
                group.setMaximumNumberOfMembers(Integer.parseInt(numberOfMembersEt.getText().toString()));
                group.setName(nameEt.getText().toString());
                group.setMembers(new ArrayList<String>());
                group.getMembers().add(HomeActivity.userId);
                group.setId(databaseReference.push().getKey());
                group.setUrl(uploadImage(group.getId()));

                databaseReference.child("groups").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        found = false;
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Group g = snapshot.getValue(Group.class);
                            if(g.getName().toLowerCase().equals(group.getName().toLowerCase())){
                                Toast.makeText(AddGroupActivity.this,"Name already taken",Toast.LENGTH_LONG).show();
                                found = true;
                                break;
                            }
                        }
                        if(!found){
                            databaseReference.child("groups").child(group.getId()).setValue(group);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
```

## Messaging other users
The Text class is used in this feature of the app. The object is pushed into the database in the child named "messageDetails" under 
the userIds of both the users.
```bash
    private void sendMessage(String toString) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String date = day + "/" + month + "/" + year;
        String time = hour + ":" + minute;

        Text text = new Text();
        text.setContent(toString);
        text.setDate(date);
        text.setTime(time);
        text.setSentBy(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String key = databaseReference.push().getKey();

        databaseReference.child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(receiverId).child(key).setValue(text);

        databaseReference.child("messages").child(receiverId)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key).setValue(text);


    }
    ```

