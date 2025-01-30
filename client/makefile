CFLAGS=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS=-lboost_system -lpthread

all: clean StompEMIClient

StompEMIClient: bin/ConnectionHandler.o bin/StompClient.o bin/StompFrame.o bin/event.o bin/summary.o bin/Report.o
		g++ -o bin/StompEMIClient bin/ConnectionHandler.o bin/StompClient.o bin/StompFrame.o bin/event.o bin/summary.o bin/Report.o $(LDFLAGS)

bin/ConnectionHandler.o: src/ConnectionHandler.cpp include/ConnectionHandler.h
		g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/event.o: src/event.cpp include/event.h include/StompFrame.h
		g++ $(CFLAGS) -o bin/event.o src/event.cpp

bin/StompClient.o: src/StompClient.cpp include/ConnectionHandler.h include/StompFrame.h include/event.h include/summary.h include/Report.h  # Removed StompClient.h
		g++ $(CFLAGS) -o bin/StompClient.o src/StompClient.cpp

bin/StompFrame.o: src/StompFrame.cpp include/StompFrame.h
		g++ $(CFLAGS) -o bin/StompFrame.o src/StompFrame.cpp

bin/summary.o: src/summary.cpp include/summary.h include/event.h
		g++ $(CFLAGS) -o bin/summary.o src/summary.cpp

bin/Report.o: src/Report.cpp include/Report.h
		g++ $(CFLAGS) -o bin/Report.o src/Report.cpp

.PHONY: clean
clean:
		rm -f bin/* StompEMIClient