<?php

include_once "class.database.php";
$db = Database::getInstance();
$mysqli = $db->getConnection();

$routeId = (int)$_POST['routeId'];
    //get user data from the database
    $res = $mysqli->query("SELECT latitude,longitude FROM AdminRoutes WHERE routeId = $routeId");
    //$obj = mysqli_fetch_object($res);
    if(mysqli_num_rows($res) > 0){
        $result = array();
        while($row=mysqli_fetch_assoc($res)){
            $result[] = $row;
        }
        $data = array(
            'status' => 1,
            'result' => $result
        );
    }else{
        $data = array(
            'status' => 0,
            'result' => ''
        );
    }
    
    //returns data as JSON format
    echo json_encode($data);

exit();
?>