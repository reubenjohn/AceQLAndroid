#AceQLAndroid

What is AceQL?
---
[AceQL](https://www.aceql.com/) is a service that will allow you to connect to remote SQL databases securely from Android and Java desktop using JDBC.

What is AceQLAndroid?
---
AceQLAndroid is an SDK for Android which will allow you to get your own Android app integrated with an AceQL Server out of the box! We'll handle everything in the background for you! So you can focus on building a beautiful app :)

Why do I need AceQL?
---
That's simply, if you have an SQL database running, and you want an android app to connect to it, then this is probably your best option. For starters, popular SQL databases such as [Oracle 11g](http://www.oracle.com/technetwork/database/database-technologies/express-edition/overview/index.html), does allow for software to connect to it by providing interfaces/connectors/drivers. But they are usually very much limited if not impossible when connecting to clients such as Android where limited connectivity is typical!

How does AceQL work?
---
When installed on the same machine that's running your SQL database, your Database Java connector *([JDBC](http://www.oracle.com/technetwork/java/javase/jdbc/index.html))* should work great in connecting AceQL to your SQL database since you won't have to rely on a network. Once that's done, AceQL acts as a web server which receives SQL queries from Android *via http(SSL/TLS)* and then makes the actual SQL query on the SQL database via JDBC. This means that it will be robust even in extreme network conditions. Since it's using the same protocol (http/https) that you're using to view this page! So any device that can browse the web is theoretically compatible with your database now!

Why do I need AceQLAndroid?
---
Although AceQL does provide easy-to-use Java SDKs for the client side, it is still not specialized for Android. You still need to know about network and UI threads, application contexts, permissions and other things that can be nasty for beginners. Which is why we've built this SDK so you don't have to worry about it.

Sneak peek into the coding:
---
Although we've made a detailed [guide](https://github.com/reubenjohn/AceQLAndroid/wiki/4.-Android-setup) on how to set up AceQLAndroid, we can't help leaving a sneak peek here:

**From ANYWHERE in your application:**

    AceQLDBManager.initialize("jdbc:aceql:http://server_ip_here:9090/ServerSqlManager","username","password"); //Only executed once in your app
    
    //Create the list you want to insert
    List<Question> listToInsert = new ArrayList<>();
    listToInsert.add(question); //Let's assume you've already instantiated this object and you want to add it to the list
    //Also make sure that it is a list of a class that implements 'SQLEntity' (For example: https://gist.github.com/reubenjohn/bd77165d97a1d1edadeb)
    
    //Specify what you want to do when the list is inserted:
    OnUpdateCompleteListener whatToDoOnCompletion = new OnUpdateCompleteListener() {
    @Override
    public void onUpdateComplete(int result, SQLException e) {
        if (e != null) {
            Toast.makeText(getActivity(),"Something's gone wrong",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } else {
            Toast.makeText(getActivity(),"Yay! Inserted: "+result+" items",Toast.LENGTH_SHORT).show();
        }
    }
    };
    
    //Now simply call the function and pass the parameters
    AceQLDBManager.insertSQLEntityList(listToInsert, whatToDoOnCompletion);

How does AceQLAndroid work?
---
AceQLAndroid is technically an open-sourced Android Studio module that will be used by your app to connect to your AceQL database with the minimum amount of effort from your side.  
It does so by handling using the AceQL client libraries in a way that conforms to the Android framework and conventions such as:

 - **Multi-threading**: Performing network actions on background threads and UI actions on UI threads
 - **Permissions**: using the right permissions
 - **Global database handler**: Allowing you to access your database from almost anywhere in your app's code
 - **Handling network configurations and failures**: We'll let your app easily listen for network issues so you can update your UI.
 - And more

This way, you don't have to get your hands dirty allowing you to quickly access your database and focus more on your app.
