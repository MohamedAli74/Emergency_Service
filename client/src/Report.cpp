#include "Report.h"

// Constructor with parameters
Report::Report(std::string city, std::string date_time, std::string event_name, std::string summary)
    : city(city), date_time(date_time), event_name(event_name), summary(summary) {}

// Default constructor
Report::Report() : city(""), date_time(""), event_name(""), summary("") {}

// Destructor
Report::~Report() {}

// Getter for city
const std::string &Report::get_city() const {
    return city;
}

// Getter for date_time
const std::string &Report::get_date_time() const {
    return date_time;
}

// Getter for event_name
const std::string &Report::get_event_name() const {
    return event_name;
}

// Getter for summary
const std::string &Report::get_summary() const {
    return summary;
}

// Setter for summary
void Report::set_summary(std::string summary) {
    this->summary = summary;
}