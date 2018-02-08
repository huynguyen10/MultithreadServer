/* @file     multithread_server.cpp
 * @author   huynguyen
 * @desc     C++ socket server example, handles multiple clients using threads.
 *
 *           Compile:
 *               g++ multithread_server.cpp -lpthread -o multithread_server
 *
 * Copyright (c) 2018, Distributed Embedded Systems (CCS Labs)
 * All rights reserved.
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <arpa/inet.h>   //inet_addr
#include <unistd.h>      //read, write
#include <pthread.h>     //for threading , link with lpthread
#include <cerrno>

#define PORT 5001

char buf[2048] = {0};

//the thread function
void *connection_handler(void *);

int main(int argc , char *argv[]) {
    int opt=1;
    int sockfd , cli_sockfd, n;
    struct sockaddr_in serv_addr, cli_addr;
    
    //Create socket
    sockfd = socket(AF_INET , SOCK_STREAM , 0);
    if (sockfd < 0) {
        printf("create socket error");
        exit(EXIT_FAILURE);
    }
    
    // Forcefully attaching socket to the port 8080
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt))) {
        perror("setsockopt error");
        exit(EXIT_FAILURE);
    }
    
    //Prepare the sockaddr_in structure
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(PORT);
    
    //Bind
    if ( bind(sockfd,(struct sockaddr *)&serv_addr , sizeof(serv_addr)) < 0) {
        //print the error message
        perror("bind error");
        exit(EXIT_FAILURE);
    }
    
    //Listen (sockfd, backlog)
    //backlog: maximum length to which the queue of pending connections
    if (listen(sockfd, 3) < 0) {
        perror("listen error");
        exit(EXIT_FAILURE);
    }
    
    //Accept and incoming connection
    puts("Waiting for incoming connections...");
    
    int cli_len = sizeof(cli_addr);
    
    while (1) {
        cli_sockfd = accept(sockfd, (struct sockaddr *)&cli_addr, (socklen_t*)&cli_len);
        if (cli_sockfd < 0) {
            perror("accept error");
        } else {
            pthread_t thread_id;
            if ( pthread_create(&thread_id, NULL, connection_handler, (void*) &cli_sockfd) < 0) {
                perror("create thread error");
            }
        }
    }
    
    //Close socket from server side
    close(sockfd);
    return 0;
}

/*
 * This will handle connection for each client
 */
void *connection_handler(void *sockfd)
{
    printf("Thread No.: %d\n", (int) pthread_self());
    
    //Get the socket descriptor
    int sock = *(int*)sockfd;
    int read_size;
    char client_buf[2000] = {0};
    
    strcpy(buf, "Hello from server");
    if ( write(sock , buf , strlen(buf)) < 0) {
        perror("write error");
        goto exit;
    }
    
    while (1) {
        //Clear buf
        bzero(client_buf, 2000);
        
        //Wait for a message to arrive
        read_size = read(sock, client_buf, 2000);
        if (read_size < 0) {
            perror("read error");
            break;
        } else if (read_size == 0) {
            printf("Connection closed\n");
            break;
        } else {
            printf("%s\n", client_buf);
            //Message echo
            if (write(sock , client_buf , strlen(client_buf)) < 0) {
                perror("write error");
                break;
            }
        }
    }
    
exit:
    printf("connection_handler ended\n");
    close(sock);
    return 0;
}
