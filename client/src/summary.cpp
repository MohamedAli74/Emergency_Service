#include "../include/summary.h"

    Summary::Summary(std::string channel_name, int Total, int active, int forces_arrival_at_scene, std::vector<Report> Events_Report)
        : channel_name(channel_name), Total(Total), active(active), forces_arrival_at_scene(forces_arrival_at_scene), Events_Report(Events_Report) {}

    Summary::Summary() : channel_name(""), Total(0), active(0), forces_arrival_at_scene(0), Events_Report() {}

    Summary::~Summary() {}

    const std::string& Summary::get_channel_name() const{
        return channel_name;
    }
    int Summary::get_Total() const{
        return Total;
    }
    int Summary::get_active() const{
        return active;
    }
    int Summary::get_forces_arrival_at_scene() const{
        return forces_arrival_at_scene;
    }
    const std::vector<Report>& Summary::get_Events_Report() const{
        return Events_Report;
    }
    
    Summary createSummary(const std::vector<Event>& event_strings, const std::string& channel_name){
        Summary ouput;
        int activeCounter = 0;
        int forcesCounter = 0;
        std::vector<Report> Events_Report;

        for (size_t i = 0; i < event_strings.size(); i++){
            Event event = event_strings.at(i);
            if(event.get_general_information().at("active") == "true"){
                activeCounter++;
            }
            if(event.get_general_information().at("forces arrival at scene") == "true"){
                forcesCounter++;
            }
            Report report(event.get_city(), epochToDateTime(event.get_date_time()), event.get_name(), event.get_description());
            Events_Report.push_back(report);
        }
        ouput = Summary(channel_name, event_strings.size(), activeCounter, forcesCounter, Events_Report);
        return ouput;
    }

    	std::string epochToDateTime(int epoch) {
		time_t time = epoch;
		struct tm *timeinfo;
		timeinfo = localtime(&time);
		char buffer[80];
		strftime(buffer, 80, "%d/%m/%y_%H:%M", timeinfo);
		return buffer;
	}
