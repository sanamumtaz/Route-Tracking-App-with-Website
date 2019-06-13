<?php
include_once "class.database.php";

$latitude = $_POST['latitude'];
$longitude = $_POST['longitude'];
$routeId = $_POST['routeId'];
$db = Database::getInstance();
$mysqli = $db->getConnection(); 
$query = "INSERT INTO AdminRoutes(routeId,latitude,longitude) VALUES('".$routeId."','".$latitude."','".$longitude."')";


if($mysqli->query($query)){
    $returnData = array(
        "status" => "ok",
        "message" => "success"
    );
}else{
    $returnData = array(
        "status" => "err",
        "message" => "failure"
    );
}

echo json_encode($returnData);
exit();
?>