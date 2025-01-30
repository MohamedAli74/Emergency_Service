#include <iostream>
using namespace std;
#include <vector>
#include <unordered_map>
#include <shared_mutex>
#include <string>
#include <memory>
#include <sstream>
#include "ConnectionHandler.h"
#include "event.h"
#include <StompFrame.h>
#include <summary.h>
#include <fstream>
#include <thread>


class StompClient
{
	private:
	ConnectionHandler *connectionHandler;
	unordered_map<string, string> receiptHashTable;
	unordered_map<string, string> subscriptionByChannel;
	unordered_map<string, string> subscriptionByID;
	unordered_map<string, unordered_map<string, std::vector<Event>>> usersEventsByChannel;
	string host;
	short port;
	bool isConnected;
	int receiptID;
	int id;
	string currentUser;

	public:
	
	~StompClient() {
		delete connectionHandler;
	}
	StompClient(const StompClient& other) 
		: connectionHandler(other.connectionHandler ? new ConnectionHandler(*other.connectionHandler) : nullptr),
			receiptHashTable(other.receiptHashTable),
			subscriptionByChannel(other.subscriptionByChannel),
			subscriptionByID(other.subscriptionByID),
			usersEventsByChannel(other.usersEventsByChannel),
			host(other.host),
			port(other.port),
			isConnected(other.isConnected),
			receiptID(other.receiptID),
			id(other.id),
			currentUser(other.currentUser) {}

	StompClient& operator=(const StompClient& other) {
		if (this == &other) {
			return *this;
		}
		delete connectionHandler;
		connectionHandler = other.connectionHandler ? new ConnectionHandler(*other.connectionHandler) : nullptr;
		receiptHashTable = other.receiptHashTable;
		subscriptionByChannel = other.subscriptionByChannel;
		subscriptionByID = other.subscriptionByID;
		usersEventsByChannel = other.usersEventsByChannel;
		host = other.host;
		port = other.port;
		isConnected = other.isConnected;
		receiptID = other.receiptID;
		id = other.id;
		currentUser = other.currentUser;
		return *this;
	}

	StompClient():
	connectionHandler(nullptr),
	receiptHashTable(unordered_map<string,string>()),
	subscriptionByChannel(unordered_map<string,string>()),
	subscriptionByID(unordered_map<string,string>()),
	usersEventsByChannel(unordered_map<string,unordered_map<string,std::vector<Event>>>()),
	host(),port(),isConnected(false),receiptID(0),id(0),currentUser(""){
	}

	void clinetHandeler()
	{
		cout << "entered the ClientHandler" << endl;
		string currentLine;
		while(true)
		{
			getline(cin,currentLine);
			istringstream ss(currentLine);
			string token;
			vector<string> tokens;
			while (ss >> token) {
				tokens.push_back(token);
			}

			string command = tokens[0];
			if(isConnected){
				if(command == "join"){
					if(tokens.size() != 2){
						cout << "join command needs 1 arg: {channel}" << endl ;
						
					}
					else subscribe(tokens[1]);
				}
				else if(command  == "exit"){
					if(tokens.size() != 2){
						cout << "exit command needs 1 arg: {channel}" << endl ;
						 
					}
					else unsubscribe(tokens[1]);
				}
				else if(command == "report"){
					if(tokens.size() != 2){
						cout << "report command needs 1 arg: {file}" << endl ;
						 
					}
					else report(tokens[1]);
				}
				else if(command == "summary"){
					if(tokens.size() != 4){
						cout << "summary command needs 3 arg: {channel_name} {user} {file}" << endl ;
						 
					}
					else summary(tokens[1], tokens[2], tokens[3]);
				}
				else if(command == "logout"){
					if(tokens.size() != 1){
						cout << "logout command needs 0 arg" << endl ;
						 
					}
					else this->logout();
				}
				else if(command == "login"){
					cout << "The client is already logged in, log out before trying again" << endl ;
				}

				else cout << "its not valid command" << endl ;
			}
			else{
				if(command == "login"){
					if(tokens.size() != 4){
						cout << "login command needs 3 args: {host:port} {username} {password}" << endl ;
					}
					else this->login(tokens[1], tokens[2], tokens[3]);
				}
				else if(command == "join" || command == "exit" || command == "report" || command == "summary"){
					cout << "The client is not logged in, log in before trying again" << endl ;
				}
				else{
					cout << "its not valid command" << endl ;
				}
			}
		}
	}
	void login(const string& hostPort, const string& username, const string& passcode) {
		string host = hostPort.substr(0, hostPort.find(':'));
		short port = stoi(hostPort.substr(hostPort.find(':') + 1));
		this -> host = host;
		this -> port = port;
		connectionHandler = new ConnectionHandler(host,port);
		if (!connectionHandler->connect()) 
		{
			cout << "Could not connect to server" << endl;
			delete connectionHandler;
			connectionHandler = nullptr;
			isConnected = false;
		}
		else
		{
			StompFrame message = getConnectMessage(username,passcode);
			string stringMessage = message.toString();
			currentUser = username;
			thread t1([&]() { readHandeler(); });
			t1.detach();
			if (!(connectionHandler->sendLine(stringMessage)))
			{
				cout << "Could not connect to server" << endl;
				close();
			}
		}
	
	}

	void subscribe(const string& channel) {
		receiptID++;
		string receipt = to_string(receiptID);
		string id = to_string(this->id++);
		StompFrame message = getSubscribeMessage(channel, id, receipt);
		string stringMessage = message.toString();
		if (!(connectionHandler->sendLine(stringMessage))) {
			cout << "Could not connect to server" << endl;
			close();
		} else {
			receiptHashTable.insert({receipt, "joined channel " + channel + " " + id});
		}
	}
	

	void unsubscribe(const string& channel) {
		if (subscriptionByChannel.count(channel)==0){
			cout << "you cant exit from channel without joining it before" << endl;
		}else{
			string subscriptionID = subscriptionByChannel.at(channel);
			receiptID++;
			string receipt = to_string(receiptID);
			StompFrame message = getUnSubscribeMessage(subscriptionID,receipt);
			string stringMessage = message.toString();
			if (!(connectionHandler->sendLine(stringMessage))) {
				cout << "Could not connect to server" << endl;
				close();
			}
			else{
				receiptHashTable.insert({receipt,"exit channel "+channel});
			}
		}
	}

	void report(const string& file) {
		names_and_events parsed = parseEventsFile(file);
		vector<Event> detectedEvents = parsed.events;
		for (size_t i = 0; i < detectedEvents.size(); i++) {
			Event event = detectedEvents.at(i);
			string channel = event.get_channel_name();
			event.setEventOwnerUser(currentUser);
			string eventDescription = event.get_description();
			/////DELETED INITIALIZATION OF THE MAP
			string message = "user:" + currentUser + "\n"
						   + "city:" + event.get_city() + "\n"
						   + "event name:" + event.get_name() + "\n"
						   + "date time:" + to_string(event.get_date_time()) + "\n" 
						   + "general information:\n" 
						   + "    active:" + event.get_general_information().at("active") + "\n"
						   + "    forces arrival at scene:" + event.get_general_information().at("") + "\n";
			StompFrame messageFrame("SEND",{{"destination",channel}},{message});
			string stringMessage = messageFrame.toString();
			if (!(connectionHandler->sendLine(stringMessage))) {
				cout << "Could not connect to server" << endl;
				close();
			}
		}
	}

	void summary(const string& channel_name, const string& user, const string& file) {
		if (usersEventsByChannel.count(channel_name) == 0) {
			cout << "There are no events in the channel" << endl;
		} else {
			if (usersEventsByChannel.at(channel_name).count(user) == 0) {
				cout << "There are no events for the user" << endl;
			} else {
					std::vector<Event> events = usersEventsByChannel.at(channel_name).at(user);
					Summary summary = createSummary(events, channel_name);
					std::ofstream out(file);

					if (!out.is_open()) {
						cout << "Could not open file" << endl;
						return;
					}
					else{
					out << "Channel: " << summary.get_channel_name() << endl;
					out << "Stats:\n";
					out << "Total events: " << summary.get_Total() << endl;
					out << "Active events: " << summary.get_active() << endl;
					out << "Forces arrival at scene: " << summary.get_forces_arrival_at_scene() << endl;
					out << "Event Reports:\n\n";

					std::vector<Report> reports = summary.get_Events_Report();
					vector<Report> sortedReports = sort(reports);
					
					for(size_t i = 0; i < sortedReports.size(); i++) {
						Report report = sortedReports.at(i);
						if(report.get_summary().length() > 27) {
							report.set_summary(report.get_summary().substr(0, 27) + "...");
						}
						out << "Report_"+i+1 << endl;
						out << "City: " << report.get_city() << endl;
						out << "Date time: " << report.get_date_time() << endl;
						out << "Event name: " << report.get_event_name() << endl;
						out << "Summary: " << report.get_summary() << endl;
						out << "-----------------------------------\n";
					}
					out.close();
					cout << "Summary file created" << endl;
				}
			}
		}
	} 
	
	void logout() {
		receiptID++;
		string receipt = to_string(receiptID);
		StompFrame message = getDisConnectMessage(receipt);
		string stringMessage = message.toString();
		if (!(connectionHandler->sendLine(stringMessage))) {
			cout << "Could not connect to server" << endl;
			close();
		} else {
			receiptHashTable.insert({receipt, "logout"});
		}
	}


	void readHandeler()
	{
		cout << "entered the readHandeler" << endl;
		while(isConnected)
		{
			string receivedMessage;
			if(!(connectionHandler->getLine(receivedMessage)))
			{
				cout << "Could not connect to server" << endl;
				close();
				return;
			}
			else
			{
				StompFrame message(receivedMessage);
				if(message.getStompCommand() == "CONNECTED")
				{
					cout << "Login successful" << endl;
					isConnected = true;	
				}
				else if (message.getStompCommand() == "ERROR")
				{
					for(auto header : message.getHeaders())
					{
						if(header.first == "message"){
							cout << "Error: " << header.second << endl;
						}
						if(header.first == "receipt-id"){
							receiptHashTable.erase(header.second);
						}
					}
				}
				else if (message.getStompCommand() == "RECEIPT")
				{
					string rID = message.getHeaders().at(0).second;
					string receipt = receiptHashTable.at(rID);
					std::vector<std::string> words = convertTOtokensByWords2(receipt);
					if(words[0] == "joined")
					{
						subscriptionByChannel.insert({words[2],words[3]});
						subscriptionByID.insert({words[3],words[2]});
						cout << "joined channel "+words[2] << endl;
					}
					else if (words[0] == "exit")
					{
						string subscriptionID = subscriptionByChannel.at(words[2]);
						subscriptionByChannel.erase(words[2]);
						subscriptionByID.erase(subscriptionID);
						cout << "exit channel "+words[2]+" done" << endl;
					}
					else if (words[0] == "logout")
					{
						connectionHandler->close();
						cout << "logout done" << endl;
						close();
					}
					receiptHashTable.erase(rID);
				}
				else if(message.getStompCommand() == "MESSAGE")
				{
					string destination;
					for(auto header : message.getHeaders())
					{
						if(header.first == "destination")
						{
							destination = header.second;
							if(usersEventsByChannel.count(destination) == 0){
								unordered_map<string, std::vector<Event>> map;
								usersEventsByChannel.insert({destination, map});
							}
						}
						if(header.first == "message-id")
						{
							string messageID = header.second;
						}
						if(header.first == "subscription")
						{
							string subscriptionID = header.second;
						}

					}
					string frameBody = message.getFrameBody();
					Event event = Event(frameBody);
					string userName = event.getEventOwnerUser();
					usersEventsByChannel[destination][userName].push_back(event);
				}
			}
				
		}

	}
	StompFrame getConnectMessage(string username ,string passcode)
	{
		StompFrame outPut("CONNECT",{{"accept-version","1.2"},{"host","stomp.cs.bgu.ac.il"},{"login",username},{"passcode",passcode}},"");;
		return outPut; 
	}
	StompFrame getDisConnectMessage(string receipt)
	{
		StompFrame output("DISCONNECT",{{"receipt",receipt}},"");
		return output; 
	}
	StompFrame getSubscribeMessage(string channel, string id , string receipt)
	{ 
		StompFrame outPut("SUBSCRIBE",{{"destination",channel},{"id",id},{"receipt",receipt}},"");
		return outPut; 
	}
	StompFrame getUnSubscribeMessage(string id , string receipt)
	{
		StompFrame outPut("UNSUBSCRIBE",{{"id",id},{"receipt",receipt}},"");
		return outPut; 
	}


	void close()
	{
		isConnected = false;
		subscriptionByChannel.clear();
		subscriptionByID.clear();
		receiptHashTable.clear();
		usersEventsByChannel.clear();
	}

	vector<Report> sort(vector<Report> reports) {
		vector<Report> sortedReports;
		for (size_t i = 0; i < reports.size(); i++) {
			Report report = reports.at(i);
			if (sortedReports.size() == 0) {
				sortedReports.push_back(report);
			} else {
				bool added = false;
				for (size_t j = 0; j < sortedReports.size(); j++) {
					Report sortedReport = sortedReports.at(j);
					if (report.get_date_time() < sortedReport.get_date_time() || 
					   (report.get_date_time() == sortedReport.get_date_time() && report.get_event_name() < sortedReport.get_event_name())) {
						sortedReports.insert(sortedReports.begin() + j, report);
						added = true;
						break;
					}
				}
				if (!added) {
					sortedReports.push_back(report);
				}
			}
		}
		return sortedReports;
	}

    std::vector<std::string> convertTOtokensByWords2(std::string messageString){
    	std::istringstream iss(messageString); 
    	std::string word;            
    	std::vector<std::string> words; 

		while (iss >> word) {
			words.push_back(word);
		}
		return words;
	}
};


int main(int argc, char *argv[]) 
{
	cout << "entered main" << endl;
	StompClient stompClient;
	stompClient.clinetHandeler();
	return 0;
}
