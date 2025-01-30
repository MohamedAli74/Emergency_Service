#pragma once

#include <string>
#include <map>
#include <vector>
#include "Report.h"
#include "event.h"


class Summary{
private:

    std::string channel_name;
    int Total;
    int active;
    int forces_arrival_at_scene;
    std::vector<Report> Events_Report;

public:
    
    Summary(std::string channel_name, int Total, int active, int forces_arrival_at_scene, std::vector<Report> Events_Report);
    Summary();
    ~Summary();
    const std::string &get_channel_name() const;
    int get_Total() const;
    int get_active() const;
    int get_forces_arrival_at_scene() const;
    const std::vector<Report> &get_Events_Report() const;
    
};

Summary createSummary(const std::vector<Event>& event_strings, const std::string& channel_name);
std::string epochToDateTime(int epoch);
