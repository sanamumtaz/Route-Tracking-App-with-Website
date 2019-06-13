<?php
include_once "class.database.php";
$db = Database::getInstance();
$mysqli = $db->getConnection();

$res = $mysqli->query("SELECT DISTINCT routeId FROM AdminRoutes");
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
?>
