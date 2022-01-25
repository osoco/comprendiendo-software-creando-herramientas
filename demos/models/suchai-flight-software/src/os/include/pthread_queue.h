/*
Cubesat Space Protocol - A small network-layer protocol designed for Cubesats
Copyright (C) 2012 Gomspace ApS (http://www.gomspace.com)
Copyright (C) 2012 AAUSAT3 Project (http://aausat3.space.aau.dk) 

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

/*
Inspired by c-pthread-queue by Matthew Dickinson
http://code.google.com/p/c-pthread-queue/
*/

#ifndef _PTHREAD_QUEUE_H_
#define _PTHREAD_QUEUE_H_

#include <stdlib.h>
#include <stdint.h>
#include <sys/time.h>
#include <string.h>

typedef struct os_thread_queue_s {
	void * buffer;
	int size;
	int item_size;
	int items;
	int in;
	int out;
	pthread_mutex_t mutex;
	pthread_cond_t cond_full;
	pthread_cond_t cond_empty;
} os_pthread_queue_t;

#define PTHREAD_QUEUE_ERROR 0
#define PTHREAD_QUEUE_EMPTY 0
#define PTHREAD_QUEUE_FULL 0
#define PTHREAD_QUEUE_OK 1

os_pthread_queue_t * os_pthread_queue_create(int length, size_t item_size);
int os_pthread_queue_send(os_pthread_queue_t *queue, void *value, uint32_t timeout);
int os_pthread_queue_receive(os_pthread_queue_t *queue, void *buf, uint32_t timeout);

#endif 

