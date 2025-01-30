#pragma once

#include <string>
#include <vector>
#include <sstream>

class StompFrame {
private:
    std::string stompCommand;
    std::vector<std::pair<std::string, std::string>> headers;
    std::string frameBody;

public:

    StompFrame(const std::string& stringMessage);

    StompFrame(const std::string& stompCommand, const std::vector<std::pair<std::string, std::string>>& headers, const std::string& frameBody);

    std::string getStompCommand() const;

    std::vector<std::pair<std::string, std::string>> getHeaders() const;

    std::string getFrameBody() const;

    std::string toString() const;

    std::vector<std::string> convertTOtokensByWords(std::string messageString);

    std::vector<std::string> convertTOtokensByLines(std::string messageString);
};