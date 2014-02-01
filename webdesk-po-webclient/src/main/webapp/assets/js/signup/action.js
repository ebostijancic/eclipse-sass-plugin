$('.btn').button();

var loginControllers = angular.module('SttSignup', []);

loginControllers.controller('SignupController', ['$scope', '$http', function($scope, $http) {
	$scope.username = "";
	$scope.password = "";
	
	$scope.checkEmailExists() = function() {
	    $http.get("/user/" + $scope.email)., function(data, status) {
		console.log(status);
	    });
	}
	
	$scope.signup = function() {	
		data.username = $scope.username;
		data.password = $scope.password;
		$http.post('/user').success(function(data, status) {
			console.log("Success");
			console.log(data);
			console.log(status);
		}).error(function(data, status) {
			console.log("Error");
			console.log(data);
			console.log(status);
		});
	} 
}]);