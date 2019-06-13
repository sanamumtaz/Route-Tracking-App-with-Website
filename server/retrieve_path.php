<?php

include_once "class.database.php";
$db = Database::getInstance();
$mysqli = $db->getConnection();

$routeId = $_POST['routeId'];
if(!empty($_POST['routeId'])){
    //get user data from the database
    $res = $mysqli->query("SELECT longitude,latitude,strayed FROM UserLocationInformation WHERE routeId = $routeId ORDER BY loctime ASC");
    
    if(mysqli_num_rows($res) > 0){
        $result = array();
        while($row=mysqli_fetch_assoc($res)){
            $result[] = $row;
        }
        $data = array(
            'status' => 'ok',
            'result' => $result
        );
    }else{
        $data = array(
            'status' => 'err',
            'result' => ''
        );
    }
    
    //returns data as JSON format
    echo json_encode($data);  
    
}
exit();
?>