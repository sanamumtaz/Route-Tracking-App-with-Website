<?php
include_once "class.database.php";

$devId = $_POST['devId'];
$lat = $_POST['lat'];
$lng = $_POST['lng'];
$timeloc = $_POST['timeloc'];
$routeId = $_POST['routeId'];
$strayed = (int)$_POST['strayed'];
$query = "INSERT INTO UserLocationInformation(deviceId,longitude,latitude,loctime,routeId,strayed) VALUES('".$devId."','".$lng."','".$lat."','".$timeloc."','".$routeId."','".$strayed."')";
$db = Database::getInstance();
$mysqli = $db->getConnection(); 

if($mysqli->query($query)){
    $returnData = array(
        "status" => 1,
        "message" => "success"
    );
}else{
    $returnData = array(
        "status" => 0,
        "message" => "failure"
    );
}

echo json_encode($returnData);
exit();
?>