# A forum community website.(Full Stack)
## Project Overview
The mock forum project has implemented several basic functionalities including registration, login, posting, commenting, replying, private messaging, and others. It utilizes prefix trees to achieve sensitive word filtering, Redis for implementing likes and follows, and Kafka to handle sending comments, likes, follows, and other system notifications. Elasticsearch has been used to enable full-text search and keyword highlighting. Additionally, the project uses wkhtmltopdf to generate growth charts and PDFs, and it tracks website UV and DAU statistics. 
## Technology Stack
| Techniques | Versions |
| ------------- | ------------- |
| SpringBoot | 2.6.1 |
| MyBatis | 2.2.0 |
| Redis | 5.0.3 |
| Kafka | 2.7.0 |
| Elasticsearch | 7.4.2 |
| Spring Security | 5.4.5 |
| Spring Quartz | 2.3.2 |
| kaptcha | 2.3.1 |
| Thymeleaf | 2.6.1 |
| Caffeine | 2.7.0 |
| MySQL | 8.0.25 |
| JDK | 1.8 |
## Function Introduction
- We utilize Redis set and zset to implement the functionality of likes and follows respectively. Redis is also used to store login tickets and authentication codes to address the distributed session problem. Redis's advanced data type, HyperLogLog, is used to count UV (Unique Visitor), and Bitmap is used to count DAU (Daily Active User) using Bitmap.

- Kafka is employed to handle sending system notifications like comments, likes, and followers. It also transfers new posts to Elasticsearch server asynchronously while encapsulating them with events to create a powerful asynchronous messaging system.

- Elasticsearch is used to conduct global searches and add features such as keyword highlighting.

- The hot post ranking module makes use of local cache Caffeine as a first-level cache and distributed cache. We use Quartz to update the hot post ranking regularly.

- To make permission authentication and control more convenient and flexible, we use Spring Security to perform permission control rather than interceptor's interception control. We also use our own authentication scheme instead of the Security authentication process.
## System Design
![Spring drawio](https://user-images.githubusercontent.com/76798014/228204843-bffb3cb8-c6c3-43a8-8358-e353e1c2456c.png)
## Running effect
After login
![image](https://user-images.githubusercontent.com/76798014/228211230-92794b3f-0a76-4557-8a5f-5689ae44ac9e.png)
New Post
![image](https://user-images.githubusercontent.com/76798014/228211158-a8469da2-7081-4afe-8cf2-5650a8352a14.png)
Post details, comments
![image](https://user-images.githubusercontent.com/76798014/228211404-872b75dc-d31c-4015-bf92-c17f89688b15.png)
System notice
![image](https://user-images.githubusercontent.com/76798014/228212195-0fe037c7-cf89-45b5-86f4-3e4b800df98d.png)
Private Letter
![image](https://user-images.githubusercontent.com/76798014/228212352-4a2a287a-c598-4f1e-858b-e85e46d6e00d.png)
Search
![image](https://user-images.githubusercontent.com/76798014/228212516-acca20b0-8fb4-4499-ad66-398a66455e92.png)
Website statistics(DAU & UV)
![image](https://user-images.githubusercontent.com/76798014/228213498-c10826e0-1649-46bf-8aa6-438f98af877f.png)
![image](https://user-images.githubusercontent.com/76798014/228213570-6d141103-be00-4abc-8f5d-7cef8c256f86.png)






