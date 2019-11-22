if (typeof qbb == "undefined"){
	var qbb = {};
}

(function($) {
	if(typeof qbb.inf == "undefined") {

		qbb.inf = {
			service_url: "http://localhost:8080/",

			getStudies: function(searchCB){
				var apiName = "liststudy";
				qbb.inf.getAPI(qbb.inf.service_url + apiName, '', searchCB);
			},

			doQuery: function(id, study, query, searchCB){
				var apiName = "query";
				var sendObject={
					id:id,
					study: study,
					sparql: query
				};
				qbb.inf.postAPI(qbb.inf.service_url + apiName, sendObject, searchCB);
			},

			createUpDateStudy: function(study, rs, searchCB){
				var apiName = "study/" + study;
				var sendObject={
					rules: rs
				};
				qbb.inf.postAPI(qbb.inf.service_url + apiName, sendObject, searchCB);
			},

			getAPI: function(url, sendObject, cb){
				qbb.inf.ajax.doGet(sendObject, url,function(s){
					var ret = s;
					if (ret)
					{
						if (typeof cb == 'function')
							cb(ret);
					}else
					{
						if (typeof cb == 'function')
							cb();
					}
				}, function(s){
					if (typeof checkOutDataCB == 'function')checkOutDataCB();
				});
			},

			postAPI: function(url, sendObject, cb){
				qbb.inf.ajax.doPost(sendObject, url,function(s){
					var ret = s;
					if (ret)
					{
						if (typeof cb == 'function')
							cb(ret);
					}else
					{
						if (typeof cb == 'function')
							cb();
					}
				}, function(s){
					if (typeof checkOutDataCB == 'function')checkOutDataCB();
				});
			},

			ajax: {
					doGet:function(sendData, url, success,error){
						qbb.inf.ajax.doSend("Get", url, sendData,success,error);
					},
					doPost:function(sendData, url, success,error){
						qbb.inf.ajax.doSend("Post", url, sendData,success,error);
					},
					doSend:function(method,url,sendData,success,error){
						dataSuccess = function(data){
							(success)(eval(data));
						};
						if (sendData) sendData.token = "";
						jQuery.ajax({
							   type: method || "Get",
							   url: url || qbb.inf.service_url,
							   data: sendData || [],
							   cache: false,
							   dataType: "jsonp", /* use "html" for HTML, use "json" for non-HTML */
							   success: dataSuccess /* (data, textStatus, jqXHR) */ || null,
							   error: error /* (jqXHR, textStatus, errorThrown) */ || null
						});
					}
			}
		};
	}
})(jQuery);