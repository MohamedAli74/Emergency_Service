#include "StompFrame.h"

#pragma once

#include <string>
#include <vector>
#include <sstream>
#include <iostream>

    StompFrame::StompFrame(const std::string& stompCommand, const std::vector<std::pair<std::string, std::string>>& headers, const std::string& frameBody)
        : stompCommand(stompCommand), headers(headers), frameBody(frameBody) {}

    StompFrame::StompFrame(const std::string& stringMessage) {
        std::vector<std::string> lines = convertTOtokensByLines(stringMessage);
        stompCommand = lines[0];
        if(stompCommand == "CONNECTED"){
            std::string versionHeader = lines[1];
            std::string version = versionHeader.substr(8, versionHeader.length());
            headers.push_back(std::make_pair("version", version));
            frameBody = "";
        }else if(stompCommand == "MESSAGE"){
            for(int i = 1; i < lines.size(); i++){
                std::string line = lines[i];
                std::vector<std::string> words = convertTOtokensByWords(line);
                if(words[0] == "subscription:")
                    headers.push_back(std::make_pair("subscription", words[1]));
                else if(words[0] == "message-id:")
                    headers.push_back(std::make_pair("message-id", words[1]));
                else if(words[0] == "destination:")
                    headers.push_back(std::make_pair("destination", words[1]));
                else frameBody += line + "\n";
            }
        }else if(stompCommand == "RECEIPT"){
            std::string receiptID = lines[1];
            headers.push_back(std::make_pair("receipt-id", receiptID));
            frameBody = "";
        }else if(stompCommand == "ERROR"){
            for(int i = 1; i < lines.size(); i++){
                std::vector<std::string> words = convertTOtokensByWords(lines[i]);
                if(words[0] == "message:")
                    headers.push_back(std::make_pair("message", words[1]));
                if(words[0] == "receipt-id:")
                    headers.push_back(std::make_pair("receipt-id", words[1]));
                else frameBody += lines[i] + "\n";
            }
        }
    }

    std::string StompFrame::getStompCommand() const {
        return stompCommand;
    }

    std::vector<std::pair<std::string, std::string>> StompFrame::getHeaders() const {
        return headers;
    }

    std::string StompFrame::getFrameBody() const {
        return frameBody;
    }

    std::string StompFrame::toString() const {
        std::ostringstream outPut;
        outPut << stompCommand << "\n";

        for (const auto& header : headers) {
            outPut << header.first << ": " << header.second << "\n\n";
        }

        if (!frameBody.empty()) {
            outPut << frameBody << "\n";
        }

        outPut << '\0';
        return outPut.str();
    }

    static std::vector<std::string> convertTOtokensByWords(std::string messageString)
	{
    	std::istringstream iss(messageString); 
    	std::string word;            
    	std::vector<std::string> words; 

		while (iss >> word) {
			words.push_back(word);
		}
		return words;
	}
	static std::vector<std::string> convertTOtokensByLines(std::string messageString)
	{
	
		std::vector<std::string> lines;
        std::stringstream ss(messageString);
		std::string line;
		while (std::getline(ss, line)){
			lines.push_back(line);
		}
		return lines;
	}