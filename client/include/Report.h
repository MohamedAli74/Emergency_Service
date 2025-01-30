#pragma once
#include <string>

class Report{
    std::string city;
    std::string date_time;
    std::string event_name;
    std::string summary;
    public:
        Report(std::string city, std::string date_time, std::string event_name, std::string summary);
        Report();
        ~Report();
        const std::string &get_city() const;
        const std::string &get_date_time() const;
        const std::string &get_event_name() const;
        const std::string &get_summary() const;
        void set_summary(std::string summary);
        
};